// test that signing works

#include <stdlib.h> // RETURN_SUCCESS
#include "crypto_sign.h"
#include "./from-libsodium/keypair.h"
#include <stdint.h>
#include <assert.h>
#include <stdio.h>

int main(int arc, char** argv) {
    const char* message = "test";
    const unsigned int message_length = 5;

    uint8_t pk[crypto_sign_PUBLICKEYBYTES];
    uint8_t sk[crypto_sign_SECRETKEYBYTES];
    crypto_sign_keypair(pk, sk);

    unsigned char signature[crypto_sign_BYTES];
    unsigned long long signature_length;

    crypto_sign_detached(signature, &signature_length, (unsigned char*) message, message_length, sk);

    // check signiture on correct message
    assert(crypto_sign_verify_detached(signature, (unsigned char*) message, message_length, pk) == 0);

    // check signature on incorrect message
    signature[0] = 'd';
    signature[1] = 'e';
    signature[2] = 'a';
    signature[3] = 'd';
    // etc (this is probably enough mutiliation)

    assert(crypto_sign_verify_detached(signature, (unsigned char*) message, message_length, pk) == -1);

    puts("Tests successful");

    return EXIT_SUCCESS;
}

