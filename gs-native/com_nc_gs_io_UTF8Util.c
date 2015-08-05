#include "com_nc_gs_io_UTF8Util.h"
#include <limits.h>
#include <x86intrin.h>

#define GS_FORCE_SSE_ALIGNMENT 0x1
#define GS_FORCE_AVX2_ALIGNMENT 0x2
#define GS_FORCE_AVX512_ALIGNMENT 0x4
#define GS_TRY_ALIGNED_STORES 0//0x8

#if defined(__GS__AVX512__)

typedef __m512i vec;

#define GS_CHUNK_SIZE 64
#define GS_FORCE_ALIGNMENT GS_FORCE_AVX512_ALIGNMENT
#define CONTROL_MASK 0xd8//11011000

/**
 * See AVX2 for details. Mask functions on 512 bits operate on each of the four 128 bit blocks. Therefore
 * the following transformation must be employed
 *
 * __m512i={[l1,h1],[l2,h2],[l3,h3],[l4,h4]} -> {[l1,l2],[h1,h2],[l3,l4],[h3,h4]}
 *
 * This can be done on a single _mm512_permutex_epi64 instruction, since it effectively sees __m512i as 2-256 bit blocks,
 * which is like applying _mm256_permute4x64_epi64 twice.
 *
 */

#ifdef GS_FORCE_AVX512_ALIGNMENT
#define loadVec(chunk) _mm512_permutex_epi64(_mm512_load_si512(chunk))
#else
#define loadVec(chunk) _mm512_permutex_epi64(_mm512_loadu_si512(chunk))
#endif

#define storeVecU _mm512_storeu_si512
#define storeVecA _mm512_store_si512
#define computeMaskU _mm512_movepi8_mask

#define zeroExtendLow(chunk) _mm512_unpacklo_epi8(chunk,_mm512_set1_epi8(0))
#define zeroExtendHigh(chunk) _mm512_unpackhi_epi8(chunk,_mm512_set1_epi8(0))

#define blendV

#elif defined(__GS__AVX2__)

typedef __m256i vec;

#define GS_CHUNK_SIZE 32
#define GS_FORCE_ALIGNMENT 0

/*__m256i, from the point of view of unpack functions is seen as a 2-128 bit tuple [low1,high1,low2,high2], not as a 1-256 bit [Low,High].
 * When we zero-extend, e.g. the low, bits of __m256i we get [low1,low2] and not [low1,high1] as we would expect. Therefore, in order to get
 * zero-extended [low1,high1] with unpack functions we must modify the result of loads, in order to produce producing 256-bit structures
 * with interleaved low and high bits: [low1,low2,high1,high2].
 *
 * E.g., if we load the String [ABCDEFGH|IJKLMNOP|QRSTUWVX|YZ012345] the 8-byte block[IJKLMNOP] will have to be shifted to bits [128-181].
 * And [QRSTUWVX] will have to be shifted to [65,127]! Therefore we must swap low2<->high1 post loading using _mm256_permute4x64_epi64,
 * using the mask 11011000, which gives us [ABCDEFGH|QRSTUWVX|IJKLMNOP|YZ012345]. Once we apply _mm256_unpacklo_epi8 we get [ABCDEFGH|IJKLMNOP]
 * as we would expect!
 */
#define CONTROL_MASK 0xd8//11011000

#ifdef GS_FORCE_AVX2_ALIGNMENT
//#define loadVec _mm256_load_si256
#define loadVec(chunk) _mm256_permute4x64_epi64(_mm256_load_si256(chunk),CONTROL_MASK)
#else
#define loadVec(chunk) _mm256_permute4x64_epi64(_mm256_loadu_si256(chunk),CONTROL_MASK)
#endif

#define storeVecU _mm256_storeu_si256
#define storeVecA _mm256_store_si256
#define computeMaskU _mm256_movemask_epi8

#define zeroExtendLow(chunk)  _mm256_unpacklo_epi8(chunk,_mm256_set1_epi8(0))
#define zeroExtendHigh(chunk) _mm256_unpackhi_epi8(chunk,_mm256_set1_epi8(0))

#define blendV _mm256_blendv_epi8

//#define zeroExtendLow(chunk)  _mm256_unpacklo_epi8(_mm256_permute4x64_epi64(chunk,CONTROL),_mm256_set1_epi8(0))
//#define zeroExtendHigh(chunk) _mm256_unpackhi_epi8(_mm256_permute4x64_epi64(chunk,CONTROL),_mm256_set1_epi8(0))

#else /*SSE*/

#define GS_CHUNK_SIZE 16
#define GS_FORCE_ALIGNMENT GS_FORCE_SSE_ALIGNMENT

#ifdef GS_FORCE_SSE_ALIGNMENT
#define loadVec _mm_load_si128
#else
#define loadVec _mm_loadu_si128
#endif

typedef __m128i vec;


#define storeVecU _mm_storeu_si128
#define storeVecA _mm_store_si128
#define loadVecU  _mm_loadu_si128
#define computeMaskU _mm_movemask_epi8
#define zeroExtendLow(chunk) _mm_unpacklo_epi8(chunk,_mm_set1_epi8(0))
#define zeroExtendHigh(chunk) _mm_unpackhi_epi8(chunk,_mm_set1_epi8(0))

#define blendV _mm_blendv_epi8
#define addBytes(X,Y) _mm_add_epi8(X,Y)

#endif

#define IS_ALIGNED(ptr,ALIGNMENT) ((long)(&(*ptr)) & (ALIGNMENT -1) ) == 0

#define SCALAR_LOOP(c0,c1,c2,src,dst,len){\
\
while (len > 0) { \
	c0 = *src++; \
	if (c0 > 0x7F) { \
		src--; \
		break; \
	} \
	*dst++ = c0; \
	len--; \
} \
\
while (len > 0) { \
	c0 = *src++ & 0xFF; \
	if (c0 <= 0x7F) { \
		*dst++ = c0; \
	} else if ((c0 >> 4) < 14) { \
		c1 = ((*src++) & 0x3F); \
		*dst++ = ((c0 & 0x1F) << 6 | c1); \
	} else { \
		c1 = ((*src++) & 0x3F) << 6; \
		c2 = ((*src++) & 0x3F); \
		*dst++ = ((c0 & 0x0F) << 12) | c1 | c2; \
	} \
	len--; \
}\
}

JNIEXPORT jlong JNICALL Java_com_nc_gs_io_UTF8Util_utf8ToAddress(JNIEnv * env,
		jclass unused, jlong srcAddr, jlong dstAddr, jint lim) {
	unsigned short c0, c1, c2;
	char* src = (char*) srcAddr;
	unsigned short* dst = (unsigned short*) dstAddr;

	while (lim >= GS_CHUNK_SIZE) {
		vec chunk = loadVec((vec *) src);

		if (computeMaskU(chunk)) {
			break;
		}

		storeVecU((vec *) dst, zeroExtendLow(chunk));

		storeVecU((vec *) (dst + GS_CHUNK_SIZE / 2), zeroExtendHigh(chunk));

		// Advance
		dst += GS_CHUNK_SIZE;
		src += GS_CHUNK_SIZE;
		lim -= GS_CHUNK_SIZE;
	}

	unsigned short c;
	while (lim > 0) {
		c = *src++;
		if (c > 0x7F) {
			src--;
			break;
		}

		*dst++ = c;
		lim--;
	}

	while (lim > 0) {
		c0 = *src++;
		if (c0 > 0x7F) {
			src--;
			break;
		}

		*dst++ = c0;
		lim--;
	}

	while (lim > 0) {
		c0 = *src++ & 0xFF;
		if (c0 <= 0x7F) {
			*dst++ = c0;
		} else if ((c0 >> 4) < 14) {
			if (--lim < 0) {
				break;
			}
			c1 = ((*src++) & 0x3F);
			*dst++ = ((c0 & 0x1F) << 6 | c1);
		} else {
			if ((lim -= 2) < 0) {
				break;
			}
			c1 = ((*src++) & 0x3F) << 6;
			c2 = ((*src++) & 0x3F);

			*dst++ = ((c0 & 0x0F) << 12) | c1 | c2;
		}

		lim--;
	}

	return (jlong) &(*src);
}

#if GS_TRY_ALIGNED_STORES != 0

#define VECTOR_ASCII_LOOP(src,dst,len,CHUNK,VEC,LOAD,STOREU,STOREA,CM,ZEL,ZEH) {\
	if (IS_ALIGNED(dst,CHUNK)){ \
		while (len >= CHUNK) { \
			VEC chunk = LOAD((VEC * ) src); \
			if (CM(chunk)) { \
				break; \
			} \
			STOREA((VEC * ) dst, ZEL(chunk)); \
			/*This is always unaligned*/ \
			STOREU((VEC * ) (dst + (CHUNK/2)), ZEH(chunk)); \
			dst += CHUNK; \
			src += CHUNK; \
			len -= CHUNK; \
		} \
	}\
	else{ \
		while (len >= CHUNK) { \
			VEC chunk = LOAD((VEC * ) src); \
			\
			if (CM(chunk)) { \
				break; \
			} \
			STOREU((VEC * ) dst, ZEL(chunk)); \
			STOREU((VEC * ) (dst + (CHUNK/2)), ZEH(chunk)); \
			dst += CHUNK; \
			src += CHUNK; \
			len -= CHUNK; \
		} \
	}\
}\

#else

#define VECTOR_ASCII_LOOP(src,dst,len,CHUNK,VEC,LOAD,STOREU,STOREA,CM,ZEL,ZEH) {\
	while (len >= CHUNK) { \
		VEC chunk = LOAD((VEC * ) src); \
		\
		if (CM(chunk)) { \
			break; \
		} \
		STOREU((VEC * ) dst, ZEL(chunk)); \
		STOREU((VEC * ) (dst + (CHUNK/2)), ZEH(chunk)); \
		dst += CHUNK; \
		src += CHUNK; \
		len -= CHUNK; \
	} \
}\

#endif

#define vectorUtf8ToUtf16(env,A,T,CHUNK,VEC,LOAD,STOREU,STOREA,CM,ZEL,ZEH,ALIGN,rv)\
{\
	unsigned short c0, c1, c2; \
	jsize len = (*env)->GetArrayLength(env, target); \
	char* src = (char*) A; \
	jbyte* _dst = (*env)->GetPrimitiveArrayCritical(env, T, 0); \
	unsigned short* dst = (unsigned short*) &(*_dst); \
	\
	/*This will align the address so that we can use _mm_load_siXXX instead of _mm_loadu_siXXX */ \
	if(ALIGN){ \
		while (((A & (CHUNK-1)) != 0) && len > 0) { \
			c0 = *src++; \
			if (c0 > 0x7F) { \
				src--; \
				goto deopt; \
			} \
			*dst++ = c0; \
			len--; \
			A++; \
		} \
	} \
	\
	/*The Vector Loop */ \
	VECTOR_ASCII_LOOP(src,dst,len,CHUNK,VEC,LOAD,STOREU,STOREA,CM,ZEL,ZEH) \
	\
	deopt: \
	SCALAR_LOOP(c0,c1,c2,src,dst,len); \
	\
	(*env)->ReleasePrimitiveArrayCritical(env, T, _dst, JNI_ABORT); \
	\
	rv = (jlong) &(*src); \
}

#ifdef __GS__SSE3__

#define V(X) _mm_set1_epi8(X)

JNIEXPORT jlong JNICALL Java_com_nc_gs_io_UTF8Util_utf8ToArray(JNIEnv* env,
		jclass clazz, jlong address, jcharArray target) {

	unsigned short c0, c1, c2;
	jsize len = (*env)->GetArrayLength(env, target);
	char* src = (char*) address;
	jbyte* _dst = (*env)->GetPrimitiveArrayCritical(env, target, 0);
	unsigned short* dst = (unsigned short*) &(*_dst);

	const vec _V_ZERO = V(0);

	while(len>=GS_CHUNK_SIZE) {
		vec chunk = loadVecU((vec*)src);

		int asciiMask = computeMaskU(chunk);

		if (!asciiMask) {
			storeVecU((vec*)(dst), zeroExtendLow(chunk));
			storeVecU((vec*)(dst+8) , zeroExtendHigh(chunk));

			dst+=GS_CHUNK_SIZE;
			src+=GS_CHUNK_SIZE;
			len-=GS_CHUNK_SIZE;
			continue;
		}

		const vec _V_SHUFFLE = _mm_set_epi8(15,14,13,12,11,10,9,8,7,6,5,4,3,2,1,0);

		vec chunk_signed = addBytes(chunk, V(0x80));
		vec cond2 = _mm_cmplt_epi8( V(0xc2-1 -0x80), chunk_signed);
		vec state = _mm_set1_epi8(0x0 | 0x80);

		state = _mm_blendv_epi8(state , V(0x2 | 0xc0), cond2);

		vec cond3 = _mm_cmplt_epi8( V(0xe0-1 -0x80), chunk_signed);

		// Possible improvement: create a separate processing when there are
		// only 2bytes sequences
		//if (!_mm_movemask_epi8(cond3)) { /*process 2 max*/ }

		state = _mm_blendv_epi8(state , V(0x3 | 0xe0), cond3);
		vec mask3 = _mm_slli_si128(cond3, 1);

		vec cond4 = _mm_cmplt_epi8(V(0xf0-1 -0x80), chunk_signed);

		// 4 bytes sequences are not vectorizeable. Fall back to the scalar processing
		if (_mm_movemask_epi8(cond4)) {
			break;
		}

		vec count = _mm_and_si128(state, V(0x7));

		vec count_sub1 = _mm_subs_epu8(count, V(1));

		vec counts = _mm_add_epi8(count, _mm_slli_si128(count_sub1, 1));

		vec shifts = count_sub1;
		shifts = _mm_add_epi8(shifts, _mm_slli_si128(shifts, 1));
		counts = _mm_add_epi8(counts, _mm_slli_si128(_mm_subs_epu8(counts, V(0x2)), 2));
		shifts = _mm_add_epi8(shifts, _mm_slli_si128(shifts, 2));

		if (asciiMask ^ _mm_movemask_epi8(_mm_cmpgt_epi8(counts, _V_ZERO))) {
			break; // error
		}
		shifts = _mm_add_epi8(shifts, _mm_slli_si128(shifts, 4));

		if (_mm_movemask_epi8(_mm_cmpgt_epi8(_mm_sub_epi8(_mm_slli_si128(counts, 1), counts), V(1)))) {
			break; //error
		}

		shifts = _mm_add_epi8(shifts, _mm_slli_si128(shifts, 8));

		vec mask = _mm_and_si128( state, _mm_set1_epi8(0xf8));
		shifts = _mm_and_si128 (shifts , _mm_cmplt_epi8(counts, V(0x2))); // <=1

		chunk = _mm_andnot_si128(mask , chunk);// from now on, we only have usefull bits

		shifts = _mm_blendv_epi8(shifts, _mm_srli_si128(shifts, 1),
				_mm_srli_si128(_mm_slli_epi16(shifts, 7) , 1));

		vec chunk_right = _mm_slli_si128(chunk, 1);

		vec chunk_low = _mm_blendv_epi8(chunk,
				_mm_or_si128(chunk, _mm_and_si128( _mm_slli_epi16(chunk_right, 6), V(0xc0))) ,
				_mm_cmpeq_epi8(counts, V(1)) );

		vec chunk_high = _mm_and_si128(chunk , _mm_cmpeq_epi8(counts, V(2)) );

		shifts = _mm_blendv_epi8(shifts, _mm_srli_si128(shifts, 2),
				_mm_srli_si128(_mm_slli_epi16(shifts, 6) , 2));
		chunk_high = _mm_srli_epi32(chunk_high, 2);

		shifts = _mm_blendv_epi8(shifts, _mm_srli_si128(shifts, 4),
				_mm_srli_si128(_mm_slli_epi16(shifts, 5) , 4));
		chunk_high = _mm_or_si128(chunk_high,
				_mm_and_si128(_mm_and_si128(_mm_slli_epi32(chunk_right, 4), V(0xf0)),
						mask3));
		int c = _mm_extract_epi16(counts, 7);
		int source_advance = !(c & 0x0200) ? 16 : !(c & 0x02) ? 15 : 14;

		vec high_bits = _mm_and_si128(chunk_high, V(0xf8));
		if (!_mm_testz_si128(mask3,
						_mm_or_si128(_mm_cmpeq_epi8(high_bits,V(0x00)) ,
								_mm_cmpeq_epi8(high_bits,V(0xd8))) )) break;

		shifts = _mm_blendv_epi8(shifts, _mm_srli_si128(shifts, 8),
				_mm_srli_si128(_mm_slli_epi16(shifts, 4) , 8));

		chunk_high = _mm_slli_si128(chunk_high, 1);

		vec shuf = _mm_add_epi8(shifts, _V_SHUFFLE);

		chunk_low = _mm_shuffle_epi8(chunk_low, shuf);
		chunk_high = _mm_shuffle_epi8(chunk_high, shuf);
		vec utf16_low = _mm_unpacklo_epi8(chunk_low, chunk_high);
		vec utf16_high = _mm_unpackhi_epi8(chunk_low, chunk_high);
		storeVecU((vec*)(dst), utf16_low);
		storeVecU((vec*)(dst+8) , utf16_high);

		int s = _mm_extract_epi32(shifts, 3);
		int dst_advance = source_advance - (0xff & (s >> 8*(3 - 16 + source_advance)));

#if defined(__SSE4_2__)
//		const int check_mode = 5 /*_SIDD_UWORD_OPS | _SIDD_CMP_RANGES*/;
		if (_mm_cmpestrc( _mm_cvtsi64_si128(0xfdeffdd0fffffffe), 4, utf16_high, 8, 5) |
				_mm_cmpestrc( _mm_cvtsi64_si128(0xfdeffdd0fffffffe), 4, utf16_low, 8, 5)) {
			break;}
#else
		if (
				!_mm_testz_si128(_mm_cmpeq_epi8(_mm_set1_epi8(0xfd), chunk_high),
						_mm_and_si128(_mm_cmplt_epi8(_mm_set1_epi8(0xd0), chunk_low),
								_mm_cmpgt_epi8(_mm_set1_epi8(0xef), chunk_low)))
				||
				!_mm_testz_si128(_mm_cmpeq_epi8(_mm_set1_epi8(0xff), chunk_high),
						_mm_or_si128(_mm_cmpeq_epi8(_mm_set1_epi8(0xfe), chunk_low),
								_mm_cmpeq_epi8(_mm_set1_epi8(0xff), chunk_low)))
		) break;
#endif

		dst += dst_advance;
		src += source_advance;
		len -= dst_advance;
	}

	SCALAR_LOOP(c0,c1,c2,src,dst,len);

	(*env)->ReleasePrimitiveArrayCritical(env, target, _dst, JNI_ABORT);

	return (jlong) &(*src);
}

#else
JNIEXPORT jlong JNICALL Java_com_nc_gs_io_UTF8Util_utf8ToArray(JNIEnv* env,
		jclass clazz, jlong address, jcharArray target) {
	jlong rv;

	vectorUtf8ToUtf16(env, address, target, GS_CHUNK_SIZE, vec, loadVec,
			storeVecU, storeVecA, computeMaskU, zeroExtendLow, zeroExtendHigh,
			GS_FORCE_ALIGNMENT, rv);
	return rv;
}
#endif

JNIEXPORT jlong JNICALL Java_com_nc_gs_io_UTF8Util_compilationFlags(JNIEnv* env,
		jclass ignore) {

	return (jlong) (GS_FORCE_SSE_ALIGNMENT | GS_FORCE_AVX2_ALIGNMENT
			| GS_FORCE_AVX512_ALIGNMENT | GS_TRY_ALIGNED_STORES);
}
