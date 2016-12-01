# based on http://wiki.osdev.org/Makefile
CC_HOSTED := gcc
CC_ARM := arm-none-eabi-gcc

AR_HOSTED := ar
AR_ARM := $(AR_HOSTED)

SODIUM_DIR := from-libsodium
HOSTED_DIR := hosted

WARNINGS := -Wall 
INCLUDE_DIRS := $(SODIUM_DIR)/include
# don't add optimisation flags on crypto or we might not be zeroing all the memory that we think we are
CFLAGS := -std=c99 $(WARNINGS) -I $(SODIUM_DIR)/include
CFLAGS_EMBED := #-mcpu=...

# common libsodium code
SODIUM_SRC := $(shell find $(SODIUM_DIR) -type f -name "*.c")
SODIUM_HOSTED_OBJ := $(patsubst %.c,%_hosted.o,$(SODIUM_SRC))
SODIUM_HOSTED_DEP := $(shell find $(SODIUM_DIR) -type f -name "*_hosted.d")
SODIUM_EMBED_OBJ := $(patsubst %.c,%_embed.o,$(SODIUM_SRC))
SODIUM_EMBED_DEP := $(shell find $(SODIUM_DIR) -type f -name "*_embed.d")

# stuff specific to hosted code
HOSTED_SRC := $(shell find $(HOSTED_DIR)/from-libsodium -type f -name "*.c")
HOSTED_OBJ := $(patsubst %.c,%_hosted.o,$(HOSTED_SRC))
HOSTED_DEP := $(shell find $(HOSTED_DIR) -type f -name "*_hosted.d")

.PHONY: clean test

hosted-test: libminisodium_hosted.a $(HOSTED_OBJ) 
	@$(CC_HOSTED) $(CFLAGS) $^ $(HOSTED_DIR)/test.c -o $@ -L. -lminisodium_hosted

test: hosted-test
	./hosted-test

libminisodium_hosted.a: $(SODIUM_HOSTED_OBJ) 
	@$(AR_HOSTED) r $@ $?

libminisodium_embedded.a: $(SODIUM_EMBED_OBJ)
	@$(AR_HOSTED) r $@ $?

clean:
	-@rm $(wildcard $(SODIUM_HOSTED_OBJ) $(SODIUM_HOSTED_DEP) libminisodium_hosted.a $(HOSTED_OBJ) $(HOSTED_DEP) hosted-test $(SODIUM_EMBED_OBJ) $(SODIUM_EMBED_DEP) libminisodium_embedded.a hosted/test_hosted.o) > /dev/null 2>&1; true

# include automatically generated dependency files (they might not exist yet)
-include $(SODIUM_EMBED_DEP)
-include $(HOSTED_DEP)
-include $(SODIUM_HOSTED_DEP)

# complies object files and generated dependency information
%_hosted.o: %.c Makefile
	@$(CC_HOSTED) $(CFLAGS) -MMD -MP -c $< -o $@

%_embed.o: %.c Makefile
	$(CC_ARM) $(CFLAGS) $(CFLAGS_EMBED) -MMD -MP -c $< -o $@
