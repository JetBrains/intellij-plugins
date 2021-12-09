foo: foo.c
ifeq ($(DEBUG),TRUE)
	cc -g -o foo foo.c
else ifeq ($(DEBUG),FALSE)
	cc -o foo foo.c
else
	@echo "Please set DEBUG to TRUE or FALSE."
endif

ifeq ($(ARCH),x86_64)
  LIB=lib64
else
ifeq ($(ARCH),ppc64)
  LIB=lib64
else
ifeq ($(ARCH),s390x)
  LIB=lib64
endif
endif
endif


clean:
	- rm -f foo