#include "com_nc_gs_io_UTF8Util.h"
#include <emmintrin.h>
#include <smmintrin.h>
#include <immintrin.h>
#include <limits.h>

JNIEXPORT jlong JNICALL Java_com_nc_gs_io_UTF8Util_utf8ToAddress(JNIEnv * env,
		jclass unused, jlong srcAddr, jlong dstAddr, jint lim) {
	unsigned short c0, c1, c2;
	char* src = (char*) srcAddr;
	unsigned short* dst = (unsigned short*) dstAddr;

	while (lim >= 16) {
		__m128i chunk = _mm_loadu_si128((__m128i *) src);

		if (_mm_movemask_epi8(chunk)) {
			break;
		}

		// unpack the first 8 bytes, padding with zeros
		__m128i low = _mm_unpacklo_epi8(chunk, _mm_set1_epi8(0));
		// and store to the destination
		_mm_storeu_si128((__m128i *) dst, low);

		__m128i high = _mm_unpackhi_epi8(chunk, _mm_set1_epi8(0));
		_mm_storeu_si128((__m128i *) (dst + 8), high);

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

JNIEXPORT jlong JNICALL Java_com_nc_gs_io_UTF8Util_utf8ToArray(JNIEnv* env,
		jclass clazz, jlong address, jcharArray target) {

	unsigned short c0, c1, c2;
	jsize len = (*env)->GetArrayLength(env, target);
	char* src = (char*) address;
	jbyte* _dst = (*env)->GetPrimitiveArrayCritical(env, target, 0);
	unsigned short* dst = (unsigned short*) &(*_dst);

	//this should align the address so that we could call _mm_load_si128
	while (((address & 15) != 0) && len > 0) {
		c0 = *src++;
		if (c0 > 0x7F) {
			src--;
			goto deopt;
		}

		*dst++ = c0;
		len--;
		address++;
	}

	while (len >= 16) {
		__m128i chunk = _mm_loadu_si128((__m128i *) src);

		if (_mm_movemask_epi8(chunk)) {
			break;
		}

		__m128i low = _mm_unpacklo_epi8(chunk, _mm_set1_epi8(0));
		_mm_storeu_si128((__m128i *) dst, low);

		__m128i high = _mm_unpackhi_epi8(chunk, _mm_set1_epi8(0));
		_mm_storeu_si128((__m128i *) (dst + 8), high);

		dst += 16;
		src += 16;
		len -= 16;
	}

	deopt: while (len > 0) {
		c0 = *src++;
		if (c0 > 0x7F) {
			src--;
			break;
		}

		*dst++ = c0;
		len--;
	}

	while (len > 0) {
		c0 = *src++ & 0xFF;
		if (c0 <= 0x7F) {
			*dst++ = c0;
		} else if ((c0 >> 4) < 14) {
			c1 = ((*src++) & 0x3F);
			//*dst++ = ((c0 & 0x1F) << 6 | ((c0 = *src++) & 0x3F));
			*dst++ = ((c0 & 0x1F) << 6 | c1);
		} else {
			c1 = ((*src++) & 0x3F) << 6;
			c2 = ((*src++) & 0x3F);

			*dst++ = ((c0 & 0x0F) << 12) | c1 | c2;
			//*dst++ = ((c0 & 0x0F) << 12 | ((c0 = *src++) & 0x3F) << 6 | ((c0 =
			//		*src++) & 0x3F) << 0);
		}

		len--;
	}

	(*env)->ReleasePrimitiveArrayCritical(env, target, _dst, JNI_ABORT);

//   jlong rv=(jlong)&(*src);
	return (jlong) &(*src);
}
