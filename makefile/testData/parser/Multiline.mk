SRCS_C  = tar.c create.c extract.c buffer.c   \
          getoldopt.c update.c gnu.c mangle.c \
          version.c list.c names.c diffarch.c \
          port.c wildmat.c getopt.c getopt1.c \
          regex.c

.PHONY: shar
shar: $(SRCS) $(AUX) \
        another set of deps
	shar $(SRCS) $(AUX) | compress \
             > tar-`sed -e '/version_string/!d' \
             -e 's/[^0-9.]*\([0-9.]*\).*/\1/' \
             -e q \
             version.c`.shar.Z
