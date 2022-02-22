if<caret>def FOO
# The `ifdef FOO` clause
HAVE_FOO = 1
else ifdef BAR
# The `else ifdef BAR` clause
HAVE_FOO = 0
HAVE_BAR = 1
endif
