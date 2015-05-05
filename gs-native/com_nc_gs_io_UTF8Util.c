#include "com_nc_gs_io_UTF8Util.h"
#include <emmintrin.h>
#include <smmintrin.h>
#include <immintrin.h>
#include <limits.h>

//SSE Mnemonics
typedef __m128i vec16;

//#define loadVec16(src) _mm_loadu_si128(src);

#define loadVec16 _mm_lddqu_si128

#define storeVec16 _mm_storeu_si128

#define computeMaskU16 _mm_movemask_epi8

#define zeroExtendLow8(chunk) _mm_unpacklo_epi8(chunk,_mm_set1_epi8(0))

#define zeroExtendHigh8(chunk) _mm_unpackhi_epi8(chunk,_mm_set1_epi8(0))


//AVX2 Mnemonics
#ifdef GS_AVX2

typedef __m256i vec32;

#define GS_AVX2_CHUNK 32

#define loadVec32(src) _mm256_loadu_si256(src)
//#define loadVec32(src) _mm256_lddqu_si256(src)

#define storeVec32(dst,src) _mm256_storeu_si256(dst,src)

#define computeMaskU32(src) _mm256_movemask_epi8(src)

#define zeroExtendLow16(chunk) _mm256_unpacklo_epi8(chunk,_mm256_set1_epi8(0))

#define zeroExtendHigh16(chunk) _mm256_unpackhi_epi8(chunk,_mm256_set1_epi8(0))

#else

#define GS_AVX2_CHUNK 16

//Dummy AVX2 support, for now
typedef __m128i vec32;

#define loadVec32(src) _mm_loadu_si128(src);

#define storeVec32(dst,src) _mm_storeu_si128(dst, src);

#define computeMaskU32(src) _mm_movemask_epi8(src)

#define zeroExtendLow16(chunk) _mm_unpacklo_epi8(chunk,_mm_set1_epi8(0))

#define zeroExtendHigh16(chunk) _mm_unpackhi_epi8(chunk,_mm_set1_epi8(0))

#endif


//AVX512 Mnemonics

#ifdef GS_AVX512

typedef __m512i vec64;

#define GS_AVX512_CHUNK 64

#define loadVec64 _mm512_loadu_si512
#define storeVec64 _mm512_storeu_si512
#define computeMaskU64 _mm512_movepi8_mask

#define zeroExtendLow32(chunk) _mm512_unpacklo_epi8(chunk,_mm512_set1_epi8(0))

#define zeroExtendHigh32(chunk) _mm512_unpackhi_epi8(chunk,_mm512_set1_epi8(0))

#else

#define GS_AVX512_CHUNK 16

//Dummy AVX512 support, for now
typedef __m128i vec64;

#define loadVec64 _mm_loadu_si128
#define storeVec64 _mm_storeu_si128
#define computeMaskU64 _mm_movemask_epi8

#define zeroExtendLow32(chunk) _mm_unpacklo_epi8(chunk,_mm_set1_epi8(0))
#define zeroExtendHigh32(chunk) _mm_unpackhi_epi8(chunk,_mm_set1_epi8(0))

#endif

JNIEXPORT jlong JNICALL Java_com_nc_gs_io_UTF8Util_utf8ToAddress(JNIEnv * env,
		jclass unused, jlong srcAddr, jlong dstAddr, jint lim) {
	unsigned short c0, c1, c2;
	char* src = (char*) srcAddr;
	unsigned short* dst = (unsigned short*) dstAddr;

	while (lim >= 16) {
		vec16 chunk = loadVec16((vec16 *) src);

		if (_mm_movemask_epi8(chunk)) {
			break;
		}

		storeVec16((vec16 *) dst, zeroExtendLow8(chunk));

		storeVec16((vec16 *) (dst + 8), zeroExtendHigh8(chunk));

		// Advance
		dst += 16;
		src += 16;
		lim -= 16;
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

#define vectorUtf8ToUtf16(env,A,T,CHUNK,VEC,LOAD,STORE,CM,ZEL,ZEH,rv)\
{\
	unsigned short c0, c1, c2; \
	jsize len = (*env)->GetArrayLength(env, target); \
	char* src = (char*) A; \
	jbyte* _dst = (*env)->GetPrimitiveArrayCritical(env, T, 0); \
	unsigned short* dst = (unsigned short*) &(*_dst); \
	\
	/*this should align the address so that we could call _mm_load_siXXX*/ \
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
	\
	while (len >= 16) { \
		VEC chunk = LOAD((VEC * ) src); \
		\
		if (CM(chunk)) { \
			break; \
		} \
		\
		STORE((VEC * ) dst, ZEL(chunk)); \
		\
		STORE((vec16 * ) (dst + 8), ZEH(chunk)); \
		\
		dst += 16; \
		src += 16; \
		len -= 16; \
	} \
	\
	deopt: while (len > 0) { \
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
	vectorUtf8ToUtf16(env, address, target, 16, vec16, loadVec16, storeVec16,
			computeMaskU16, zeroExtendLow8, zeroExtendHigh8, rv);
	return rv;
}

JNIEXPORT jlong JNICALL Java_com_nc_gs_io_UTF8Util_utf8ToArrayAVX(JNIEnv* env,
		jclass clazz, jlong address, jcharArray target) {
	jlong rv;
	vectorUtf8ToUtf16(env, address, target, GS_AVX2_CHUNK, vec32, loadVec32, storeVec32,
			computeMaskU32, zeroExtendLow16, zeroExtendHigh16, rv);
	return rv;
}

JNIEXPORT jlong JNICALL Java_com_nc_gs_io_UTF8Util_utf8ToArrayAVX512(JNIEnv* env,
		jclass clazz, jlong address, jcharArray target) {
	jlong rv;
	vectorUtf8ToUtf16(env, address, target, 64, vec64, loadVec64, storeVec64,
			computeMaskU64, zeroExtendLow32, zeroExtendHigh32, rv);
	return rv;
}
