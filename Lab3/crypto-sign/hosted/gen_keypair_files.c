// generates a keypair_{pk,sk}.c and keypair_{pk,sk}.h files for use in embedded code

#include <stdlib.h>
#include "./from-libsodium/keypair.h"
#include <stdio.h>
#include <assert.h>
#include <stdint.h>
#include <sys/types.h>
#include <sys/stat.h>
#include <fcntl.h>
#include <unistd.h>
#include <string.h>
#include "crypto_sign.h"
#include <errno.h>

int main(int arc, char** argv) {
    puts("Writing a new keypair to keypair.c and keypair.h\n");

    // open files
    const int flags = O_WRONLY | O_CREAT | O_EXCL;
    const mode_t mode = S_IRUSR | S_IWUSR; // rw-------

    const int header_pk_fd = open("keypair_pk.h", flags, mode);
    if (header_pk_fd == -1) {
        puts("Failed to open the pk headder file\nDid you remember to delete the old files?");
        return EXIT_FAILURE;
    }

    const int source_pk_fd = open("keypair_pk.c", flags, mode);
    if (source_pk_fd == -1) {
        puts("Failed to open the pk source file\n");
        return EXIT_FAILURE;
    }

    const int header_sk_fd = open("keypair_sk.h", flags, mode);
    if (header_sk_fd == -1) {
        puts("Failed to open the sk headder file\n");
        return EXIT_FAILURE;
    }

    const int source_sk_fd = open("keypair_sk.c", flags, mode);
    if (source_sk_fd == -1) {
        puts("Failed to open the sk source file\n");
        return EXIT_FAILURE;
    }

    // write header files
    const char* header_pk_buf = "#ifndef KEYPAIR_PK_H\n#define KEYPAIR_PK_H\n\n#include<stdint.h>\n\nconst size_t public_key_bytes = 32;\nextern uint8_t pk[public_key_bytes];\n\n#endif\n";
    const size_t header_pk_buf_len = strlen(header_pk_buf);
    
    if (write(header_pk_fd, (const void*) header_pk_buf, header_pk_buf_len) != header_pk_buf_len) {
        int err = errno;
        printf("Failed to write to the pk header file. errno=%i\n", err); return EXIT_FAILURE; } const char* header_sk_buf = "#ifndef KEYPAIR_SK_H\n#define KEYPAIR_SK_H\n\n#include<stdint.h>\n\nconst size_t secret_key_bytes = 64;\nextern uint8_t sk[secret_key_bytes];\n\n#endif\n"; const size_t header_sk_buf_len = strlen(header_sk_buf); if (write(header_sk_fd, (const void*) header_sk_buf, header_sk_buf_len) != header_sk_buf_len) { int err = errno; printf("Failed to write to the sk header file. errno=%i\n", err); return EXIT_FAILURE;
    }

    // generate keys
    uint8_t pk[crypto_sign_PUBLICKEYBYTES];
    uint8_t sk[crypto_sign_SECRETKEYBYTES];
    crypto_sign_ed25519_keypair(pk, sk);	 

    // write pk source file
    const char* source_pk_start_buf = "#include <stdint.h>\n\nconst size_t public_key_bytes = 32;\n\nuint8_t pk[] = {";
    const size_t source_pk_start_len = strlen(source_pk_start_buf);

    if (write(source_pk_fd, (const void*) source_pk_start_buf, source_pk_start_len) != source_pk_start_len) {
        puts("Error writing the start of the pk source file\n");
        return EXIT_FAILURE;
    }

    for (int i = 0; i < crypto_sign_PUBLICKEYBYTES; i++) {
        char buf[6] = {'\0'};
        if (snprintf(buf, 6, "%i, ", pk[i]) == -1) {
            puts("Error snprintf'ing the public key\n");
            return EXIT_FAILURE;
        }

        size_t buf_len = strlen(buf);

        // don't add an extra comma on the last one
        if (i == (crypto_sign_PUBLICKEYBYTES - 1)) {
            buf[buf_len-2] = '\0';
            buf_len = strlen(buf);
        }

        if (write(source_pk_fd, (const void*) buf, buf_len) != buf_len) {
            puts("Error writing the public key\n");
            return EXIT_FAILURE;
        }
    }

    const char* source_end_buf = "};\n";
    const size_t source_end_len = strlen(source_end_buf);

    if (write(source_pk_fd, (const void*) source_end_buf, source_end_len) != source_end_len) {
        puts("Error writing the public key end\n");
        return EXIT_FAILURE;
    }

    // write sk source file
    const char* source_sk_start_buf = "#include <stdint.h>\n\nconst size_t secret_key_bytes = 64;\n\nuint8_t sk[] = {";
    const size_t source_sk_start_len = strlen(source_sk_start_buf);

    if (write(source_sk_fd, (const void*) source_sk_start_buf, source_sk_start_len) != source_sk_start_len) {
        puts("Error writing the scret key start\n");
        return EXIT_FAILURE;
    }

    for (int i = 0; i < crypto_sign_SECRETKEYBYTES; i++) {
        char buf[6] = {'\0'};
        if (snprintf(buf, 6, "%i, ", sk[i]) == -1) {
            puts("Error sprintf'ing the secret key\n");
            return EXIT_FAILURE;
        }

        size_t buf_len = strlen(buf);
        
        // don't add an extra comma to the last one
        if (i == (crypto_sign_SECRETKEYBYTES - 1)) {
            buf[buf_len-2] = '\0';
            buf_len = strlen(buf);
        }

        if (write(source_sk_fd, (const void*) buf, buf_len) != buf_len) {
            puts("Error writing the secret key\n");
            return EXIT_FAILURE;
        }
    }

    if (write(source_sk_fd, (const void*) source_end_buf, source_end_len) != source_end_len) {
        puts("Error writing the secret key end\n");
        return EXIT_FAILURE;
    }

    return EXIT_SUCCESS;
}
