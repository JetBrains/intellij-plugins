# Simple Makefile

all:

some: hello #end of line comment

hello world: hello.o world.o	#comment

GCC = gcc
SHELL := /bin/bash
ROUTE_TEST_TMP_FILE := $(shell mktemp)
private PARALLEL = parallel --no-notice

define hello
    some value
endef

clean: #end of line comment
	echo hello  #end of line comment
ifeq ($(BUILD_TYPE),QA)  #end of line comment
	echo world
else  #end of line comment
	rm -rf dependencies
endif  #end of line comment
	echo end

undefine hello #end of line comment

override hello = qwe

export  #end of line comment
export GCC  #end of line comment
export GCC = qwerty

vpath %.h qwerty
vpath %.h qwe asdf src
vpath %.h
vpath

.o.c:
	$(GCC) -c qwe
	echo "Hello World"

hello: world