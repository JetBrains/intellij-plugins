ifeq ($(BUILD_TYPE),QA)
GCC = gcc
else
GCC = g++
endif

clean: ;