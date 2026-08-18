F<caret>OO := one
BAR ?= $(FOO)
BAZ += $(FOO)

all: $(FOO) $(BAR) $(BAZ)
