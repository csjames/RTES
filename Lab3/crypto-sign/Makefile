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

# common libsodium code
SODIUM_SRC := $(shell find $(SODIUM_DIR) -type f -name "*.c")
SODIUM_HOSTED_OBJ := $(patsubst %.c,%_hosted.o,$(SODIUM_SRC))
SODIUM_HOSTED_DEP := $(shell find $(SODIUM_DIR) -type f -name "*_hosted.d")

# stuff specific to hosted code
HOSTED_SRC := $(shell find $(HOSTED_DIR) -type f -name "*.c")
HOSTED_OBJ := $(patsubst %.c,%_hosted.o,$(HOSTED_SRC))
HOSTED_DEP := $(shell find $(HOSTED_DIR) -type f -name "*_hosted.d")

.PHONY: clean test

hosted-test: libminisodium_hosted.a $(HOSTED_OBJ) 
	@$(CC_HOSTED) $(CFLAGS) $^ -o $@ -L. -lminisodium_hosted

test: hosted-test
	./hosted-test

libminisodium_hosted.a: $(SODIUM_HOSTED_OBJ) 
	@$(AR_HOSTED) r $@ $?

clean:
	-@rm $(wildcard $(SODIUM_HOSTED_OBJ) $(SODIUM_HOSTED_DEP) libminisodium_hosted.a $(HOSTED_OBJ) $(HOSTED_DEP) hosted-test)

# include automatically generated dependency files (they might not exist yet)
-include $(SODIUM_DEP)
-include $(HOSTED_DEP)

# complies object files and generated dependency information
%_hosted.o: %.c Makefile
	@$(CC_HOSTED) $(CFLAGS) -MMD -MP -c $< -o $@


