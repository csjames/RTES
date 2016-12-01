// test that signing works

#include "crypto_sign.h"
#include <stdint.h>
#include "keypair_pk.h"
#include "keypair_sk.h"
#include <stdbool.h>

// TODO: gpio or turn on an LED or something?
void assert(bool p) {
    if (p) {
        while(true);
    }
}

int main(int arc, char** argv) {
    const char* message = "test";
    const unsigned int message_length = 5;

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

    // TODO gpio or LED or something?
    //(all successful if we get to here)
    while(true);

    return 0;
}

