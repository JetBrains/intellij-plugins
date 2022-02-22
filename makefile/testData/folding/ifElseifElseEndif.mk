# ifdef..else ifdef..else..endif
ifdef <fold text='FOO'>FOO
# The `ifdef FOO` clause
<fold text='HAVE_FOO=1'>HAVE_FOO = 1</fold>
else <fold text='ifdef BAR '>ifdef BAR
# The `else ifdef BAR` clause
<fold text='HAVE_FOO=0'>HAVE_FOO = 0</fold>
<fold text='HAVE_BAR=1'>HAVE_BAR = 1</fold>
</fold>else<fold text=' HAVE_FOO = 0
HAVE_BAR = 0'>
# The `else` clause
<fold text='HAVE_FOO=0'>HAVE_FOO = 0</fold>
<fold text='HAVE_BAR=0'>HAVE_BAR = 0</fold>
endif</fold></fold>


# ifndef..else ifndef..else..endif
ifndef <fold text='FOO'>FOO
# The `ifndef FOO` clause
<fold text='HAVE_FOO=0'>HAVE_FOO = 0</fold>
else <fold text='ifndef BAR '>ifndef BAR
# The `else ifndef BAR` clause
<fold text='HAVE_FOO=1'>HAVE_FOO = 1</fold>
<fold text='HAVE_BAR=0'>HAVE_BAR = 0</fold>
</fold>else<fold text=' HAVE_FOO = 1
HAVE_BAR = 1'>
# The `else` clause
<fold text='HAVE_FOO=1'>HAVE_FOO = 1</fold>
<fold text='HAVE_BAR=1'>HAVE_BAR = 1</fold>
endif</fold></fold>
