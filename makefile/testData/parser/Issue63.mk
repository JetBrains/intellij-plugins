uname := $(shell uname -s)
ifneq ($(findstring $(shell uname -r), Microsoft),)
	osinclude := windows.mk
else ifeq ($(uname),Linux)
	osinclude := linux.mk
else ifeq ($(uname),Darwin)
	osinclude := osx.mk
endif

include ./includes/$(osinclude)