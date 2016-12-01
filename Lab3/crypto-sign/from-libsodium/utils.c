#ifndef __STDC_WANT_LIB_EXT1__
# define __STDC_WANT_LIB_EXT1__ 1
#endif
#include <assert.h>
#include <errno.h>
#include <limits.h>
//#include <signal.h>
#include <stddef.h>
#include <stdint.h>
#include <stdlib.h>
#include <string.h>

#include "utils.h"

#define CANARY_SIZE 16U
#define GARBAGE_VALUE 0xdb

#ifndef MAP_NOCORE
# define MAP_NOCORE 0
#endif
#if !defined(MAP_ANON) && defined(MAP_ANONYMOUS)
# define MAP_ANON MAP_ANONYMOUS
#endif
#if defined(WINAPI_DESKTOP) || (defined(MAP_ANON) && defined(HAVE_MMAP)) || defined(HAVE_POSIX_MEMALIGN)
# define HAVE_ALIGNED_MALLOC
#endif
#if defined(HAVE_MPROTECT) && !(defined(PROT_NONE) && defined(PROT_READ) && defined(PROT_WRITE))
# undef HAVE_MPROTECT
#endif
#if defined(HAVE_ALIGNED_MALLOC) && (defined(WINAPI_DESKTOP) || defined(HAVE_MPROTECT))
# define HAVE_PAGE_PROTECTION
#endif
#if !defined(MADV_DODUMP) && defined(MADV_CORE)
# define MADV_DODUMP   MADV_CORE
# define MADV_DONTDUMP MADV_NOCORE
#endif

//static size_t page_size;
//static unsigned char canary[CANARY_SIZE];

#ifdef HAVE_WEAK_SYMBOLS
__attribute__ ((weak)) void
_sodium_memzero_as_a_weak_symbol_to_prevent_lto(void * const pnt, const size_t len)
{
    unsigned char *pnt_ = (unsigned char *) pnt;
    size_t         i = (size_t) 0U;

    while (i < len) {
        pnt_[i++] = 0U;
    }
}
#endif

void
sodium_memzero(void * const pnt, const size_t len)
{
#ifdef _WIN32
    SecureZeroMemory(pnt, len);
#elif defined(HAVE_MEMSET_S)
    if (len > 0U && memset_s(pnt, (rsize_t) len, 0, (rsize_t) len) != 0) {
        abort(); /* LCOV_EXCL_LINE */
    }
#elif defined(HAVE_EXPLICIT_BZERO)
    explicit_bzero(pnt, len);
#elif HAVE_WEAK_SYMBOLS
    _sodium_memzero_as_a_weak_symbol_to_prevent_lto(pnt, len);
#else
    volatile unsigned char *volatile pnt_ =
        (volatile unsigned char * volatile) pnt;
    size_t i = (size_t) 0U;

    while (i < len) {
        pnt_[i++] = 0U;
    }
#endif
}

#ifdef HAVE_WEAK_SYMBOLS
__attribute__ ((weak)) void
_sodium_dummy_symbol_to_prevent_memcmp_lto(const unsigned char *b1,
                                           const unsigned char *b2,
                                           const size_t len)
{
    (void) b1;
    (void) b2;
    (void) len;
}
#endif

int
sodium_memcmp(const void * const b1_, const void * const b2_, size_t len)
{
#ifdef HAVE_WEAK_SYMBOLS
    const unsigned char *b1 = (const unsigned char *) b1_;
    const unsigned char *b2 = (const unsigned char *) b2_;
#else
    const volatile unsigned char *volatile b1 =
        (const volatile unsigned char * volatile) b1_;
    const volatile unsigned char *volatile b2 =
        (const volatile unsigned char * volatile) b2_;
#endif
    size_t               i;
    unsigned char        d = (unsigned char) 0U;

#if HAVE_WEAK_SYMBOLS
    _sodium_dummy_symbol_to_prevent_memcmp_lto(b1, b2, len);
#endif
    for (i = 0U; i < len; i++) {
        d |= b1[i] ^ b2[i];
    }
    return (1 & ((d - 1) >> 8)) - 1;
}

#ifdef HAVE_WEAK_SYMBOLS
__attribute__ ((weak)) void
_sodium_dummy_symbol_to_prevent_compare_lto(const unsigned char *b1,
                                            const unsigned char *b2,
                                            const size_t len)
{
    (void) b1;
    (void) b2;
    (void) len;
}
#endif

int
sodium_compare(const unsigned char *b1_, const unsigned char *b2_, size_t len)
{
#ifdef HAVE_WEAK_SYMBOLS
    const unsigned char *b1 = b1_;
    const unsigned char *b2 = b2_;
#else
    const volatile unsigned char * volatile b1 =
        (const volatile unsigned char * volatile) b1_;
    const volatile unsigned char * volatile b2 =
        (const volatile unsigned char * volatile) b2_;
#endif
    unsigned char gt = 0U;
    unsigned char eq = 1U;
    size_t        i;

#if HAVE_WEAK_SYMBOLS
    _sodium_dummy_symbol_to_prevent_compare_lto(b1, b2, len);
#endif
    i = len;
    while (i != 0U) {
        i--;
        gt |= ((b2[i] - b1[i]) >> 8) & eq;
        eq &= ((b2[i] ^ b1[i]) - 1) >> 8;
    }
    return (int) (gt + gt + eq) - 1;
}

int
sodium_is_zero(const unsigned char *n, const size_t nlen)
{
    size_t        i;
    unsigned char d = 0U;

    for (i = 0U; i < nlen; i++) {
        d |= n[i];
    }
    return 1 & ((d - 1) >> 8);
}

void
sodium_increment(unsigned char *n, const size_t nlen)
{
    size_t        i = 0U;
    uint_fast16_t c = 1U;

#ifdef HAVE_AMD64_ASM
    uint64_t      t64, t64_2;
    uint32_t      t32;

    if (nlen == 12U) {
        __asm__ __volatile__("xorq %[t64], %[t64] \n"
                             "xorl %[t32], %[t32] \n"
                             "stc \n"
                             "adcq %[t64], (%[out]) \n"
                             "adcl %[t32], 8(%[out]) \n"
                             : [t64] "=&r"(t64), [t32] "=&r" (t32)
                             : [out] "D"(n)
                             : "memory", "flags", "cc");
        return;
    } else if (nlen == 24U) {
        __asm__ __volatile__("movq $1, %[t64] \n"
                             "xorq %[t64_2], %[t64_2] \n"
                             "addq %[t64], (%[out]) \n"
                             "adcq %[t64_2], 8(%[out]) \n"
                             "adcq %[t64_2], 16(%[out]) \n"
                             : [t64] "=&r"(t64), [t64_2] "=&r" (t64_2)
                             : [out] "D"(n)
                             : "memory", "flags", "cc");
        return;
    } else if (nlen == 8U) {
        __asm__ __volatile__("incq (%[out]) \n"
                             :
                             : [out] "D"(n)
                             : "memory", "flags", "cc");
        return;
    }
#endif
    for (; i < nlen; i++) {
        c += (uint_fast16_t) n[i];
        n[i] = (unsigned char) c;
        c >>= 8;
    }
}

void
sodium_add(unsigned char *a, const unsigned char *b, const size_t len)
{
    size_t        i = 0U;
    uint_fast16_t c = 0U;

#ifdef HAVE_AMD64_ASM
    uint64_t      t64, t64_2, t64_3;
    uint32_t      t32;

    if (len == 12U) {
        __asm__ __volatile__("movq (%[in]), %[t64] \n"
                             "movl 8(%[in]), %[t32] \n"
                             "addq %[t64], (%[out]) \n"
                             "adcl %[t32], 8(%[out]) \n"
                             : [t64] "=&r"(t64), [t32] "=&r" (t32)
                             : [in] "S"(b), [out] "D"(a)
                             : "memory", "flags", "cc");
        return;
    } else if (len == 24U) {
        __asm__ __volatile__("movq (%[in]), %[t64] \n"
                             "movq 8(%[in]), %[t64_2] \n"
                             "movq 16(%[in]), %[t64_3] \n"
                             "addq %[t64], (%[out]) \n"
                             "adcq %[t64_2], 8(%[out]) \n"
                             "adcq %[t64_3], 16(%[out]) \n"
                             : [t64] "=&r"(t64), [t64_2] "=&r"(t64_2), [t64_3] "=&r"(t64_3)
                             : [in] "S"(b), [out] "D"(a)
                             : "memory", "flags", "cc");
        return;
    } else if (len == 8U) {
        __asm__ __volatile__("movq (%[in]), %[t64] \n"
                             "addq %[t64], (%[out]) \n"
                             : [t64] "=&r"(t64)
                             : [in] "S"(b), [out] "D"(a)
                             : "memory", "flags", "cc");
        return;
    }
#endif
    for (; i < len; i++) {
        c += (uint_fast16_t) a[i] + (uint_fast16_t) b[i];
        a[i] = (unsigned char) c;
        c >>= 8;
    }
}

/* Derived from original code by CodesInChaos */
char *
sodium_bin2hex(char * const hex, const size_t hex_maxlen,
               const unsigned char * const bin, const size_t bin_len)
{
    size_t       i = (size_t) 0U;
    unsigned int x;
    int          b;
    int          c;

    if (bin_len >= SIZE_MAX / 2 || hex_maxlen <= bin_len * 2U) {
        abort(); /* LCOV_EXCL_LINE */
    }
    while (i < bin_len) {
        c = bin[i] & 0xf;
        b = bin[i] >> 4;
        x = (unsigned char) (87U + c + (((c - 10U) >> 8) & ~38U)) << 8 |
            (unsigned char) (87U + b + (((b - 10U) >> 8) & ~38U));
        hex[i * 2U] = (char) x;
        x >>= 8;
        hex[i * 2U + 1U] = (char) x;
        i++;
    }
    hex[i * 2U] = 0U;

    return hex;
}

int
sodium_hex2bin(unsigned char * const bin, const size_t bin_maxlen,
               const char * const hex, const size_t hex_len,
               const char * const ignore, size_t * const bin_len,
               const char ** const hex_end)
{
    size_t        bin_pos = (size_t) 0U;
    size_t        hex_pos = (size_t) 0U;
    int           ret = 0;
    unsigned char c;
    unsigned char c_acc = 0U;
    unsigned char c_alpha0, c_alpha;
    unsigned char c_num0, c_num;
    unsigned char c_val;
    unsigned char state = 0U;

    while (hex_pos < hex_len) {
        c = (unsigned char) hex[hex_pos];
        c_num = c ^ 48U;
        c_num0 = (c_num - 10U) >> 8;
        c_alpha = (c & ~32U) - 55U;
        c_alpha0 = ((c_alpha - 10U) ^ (c_alpha - 16U)) >> 8;
        if ((c_num0 | c_alpha0) == 0U) {
            if (ignore != NULL && state == 0U && strchr(ignore, c) != NULL) {
                hex_pos++;
                continue;
            }
            break;
        }
        c_val = (c_num0 & c_num) | (c_alpha0 & c_alpha);
        if (bin_pos >= bin_maxlen) {
            ret = -1;
            errno = ERANGE;
            break;
        }
        if (state == 0U) {
            c_acc = c_val * 16U;
        } else {
            bin[bin_pos++] = c_acc | c_val;
        }
        state = ~state;
        hex_pos++;
    }
    if (state != 0U) {
        hex_pos--;
    }
    if (hex_end != NULL) {
        *hex_end = &hex[hex_pos];
    }
    if (bin_len != NULL) {
        *bin_len = bin_pos;
    }
    return ret;
}

