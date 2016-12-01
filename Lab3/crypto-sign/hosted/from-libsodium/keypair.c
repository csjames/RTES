// from libsodium
#include <string.h>

//#include "crypto_sign_ed25519.h"
#include "crypto_hash_sha512.h"
#include "crypto_scalarmult_curve25519.h"
#include "./randombytes.h"
#include "utils.h"
#include "curve25519_ref10.h"

int crypto_sign_ed25519_seed_keypair(unsigned char *pk, unsigned char *sk,
                                     const unsigned char *seed)
{
    ge_p3 A;

    crypto_hash_sha512(sk,seed,32);
    sk[0] &= 248;
    sk[31] &= 63;
    sk[31] |= 64;

    ge_scalarmult_base(&A,sk);
    ge_p3_tobytes(pk,&A);

    memmove(sk, seed, 32);
    memmove(sk + 32, pk, 32);
    return 0;
}

int crypto_sign_ed25519_keypair(unsigned char *pk, unsigned char *sk)
{
    unsigned char seed[32];
    int           ret;

    randombytes_buf(seed, sizeof seed);
    ret = crypto_sign_ed25519_seed_keypair(pk, sk, seed);
    sodium_memzero(seed, sizeof seed);

    return ret;
}
