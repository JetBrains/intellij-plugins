# ifeq..else ifeq..endif
ifeq <fold text='(arg1, arg2)'>(arg1, arg2)
# The 1st `ifeq` clause
else <fold text='ifeq (arg1, arg3) '>ifeq (arg1, arg3)
# The 2nd `ifeq` clause
<fold text='VAR:=value'>VAR := value</fold>
endif</fold></fold>


# ifneq..else ifneq..endif
ifneq <fold text='(arg1, arg2)'>(arg1, arg2)
# The 1st `ifneq` clause
else <fold text='ifneq (arg1, arg3) '>ifneq (arg1, arg3)
# The 2nd `ifneq` clause
<fold text='VAR:=value'>VAR := value</fold>
endif</fold></fold>


# ifdef..else ifdef..endif
ifdef <fold text='FOO'>FOO
# The `ifdef FOO` clause
else <fold text='ifdef BAR '>ifdef BAR
# The `else ifdef BAR` clause
<fold text='VAR:=value'>VAR := value</fold>
endif</fold></fold>


# ifndef..else ifndef..endif
ifndef <fold text='FOO'>FOO
# The `ifndef FOO` clause
else <fold text='ifndef BAR '>ifndef BAR
# The `else ifndef BAR` clause
<fold text='VAR:=value'>VAR := value</fold>
endif</fold></fold>
