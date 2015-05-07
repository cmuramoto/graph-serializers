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

#ifdef GS_FORCE_AVX512_ALIGNMENT
#define loadVec _mm512_load_si512
#else
#define loadVec _mm512_loadu_si512
#endif


#define storeVecU _mm512_storeu_si512
#define storeVecA _mm512_store_si512
#define computeMaskU _mm512_movepi8_mask

//This is probably wrong! See __AVX2__ details.
#define zeroExtendLow(chunk) _mm512_unpacklo_epi8(chunk,_mm512_set1_epi8(0))
#define zeroExtendHigh(chunk) _mm512_unpackhi_epi8(chunk,_mm512_set1_epi8(0))

#elif defined(__GS__AVX2__)

typedef __m256i vec;

#define GS_CHUNK_SIZE 32
#define GS_FORCE_ALIGNMENT 0

/*__m256i load by _mm256_load[u]_si256 takes the form of a 2-128 bit tuple with interleaved low and high bits: [low1,low2,high1,high2]
 *
 * E.g., if we load the String [ABCDEFGH|IJKLMNOP|QRSTUWVX|YZ012345] the [IJKLMNOP] will be stored at high1, bits [128-181]. And [QRSTUWVX]
 * will be stored at low2! Therefore we must swap low2<->high1 post loading using _mm256_permute4x64_epi64, using the mask 11011000, which
 * gives us [low1,high1,low2,high2].
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
#define computeMaskU _mm_movemask_epi8
#define zeroExtendLow(chunk) _mm_unpacklo_epi8(chunk,_mm_set1_epi8(0))
#define zeroExtendHigh(chunk) _mm_unpackhi_epi8(chunk,_mm_set1_epi8(0))

#endif

#define IS_ALIGNED(ptr,ALIGNMENT) ((long)(&(*ptr)) & (ALIGNMENT -1) ) == 0

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

#define vectorAsciiLoop(src,dst,len,CHUNK,VEC,LOAD,STOREU,STOREA,CM,ZEL,ZEH) {\
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

#define vectorAsciiLoop(src,dst,len,CHUNK,VEC,LOAD,STOREU,STOREA,CM,ZEL,ZEH) {\
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
	vectorAsciiLoop(src,dst,len,CHUNK,VEC,LOAD,STOREU,STOREA,CM,ZEL,ZEH) \
	\
	deopt: \
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
	\
	(*env)->ReleasePrimitiveArrayCritical(env, T, _dst, JNI_ABORT); \
	\
	rv = (jlong) &(*src); \
}

JNIEXPORT jlong JNICALL Java_com_nc_gs_io_UTF8Util_utf8ToArray(JNIEnv* env,
		jclass clazz, jlong address, jcharArray target) {
	jlong rv;

	vectorUtf8ToUtf16(env, address, target, GS_CHUNK_SIZE, vec, loadVec,
			storeVecU, storeVecA, computeMaskU, zeroExtendLow,
			zeroExtendHigh, GS_FORCE_ALIGNMENT, rv);
	return rv;
}

JNIEXPORT jlong JNICALL Java_com_nc_gs_io_UTF8Util_compilationFlags(JNIEnv* env,
		jclass ignore) {

	return (jlong) (GS_FORCE_SSE_ALIGNMENT | GS_FORCE_AVX2_ALIGNMENT
			| GS_FORCE_AVX512_ALIGNMENT | GS_TRY_ALIGNED_STORES);
}
