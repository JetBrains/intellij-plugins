# ifeq..else..endif
ifeq <fold text='(arg1, arg2)'>(arg1, arg2)
# Block body
else<fold text=' ...'>
# Block body
endif</fold></fold>

ifeq <fold text='(arg1, arg2)'>(arg1, arg2)
<fold text='VAR=value'>VAR = value</fold>
else<fold text=' VAR = value'>
<fold text='VAR=value'>VAR = value</fold>
endif</fold></fold>


# ifneq..else..endif
ifneq <fold text='(arg1, arg2)'>(arg1, arg2)
# Block body
else<fold text=' ...'>
# Block body
endif</fold></fold>

ifneq <fold text='(arg1, arg2)'>(arg1, arg2)
<fold text='VAR=value'>VAR = value</fold>
else<fold text=' VAR = value'>
<fold text='VAR=value'>VAR = value</fold>
endif</fold></fold>


# ifdef..else..endif
ifdef <fold text='variable-name'>variable-name
# Block body
else<fold text=' ...'>
# Block body
endif</fold></fold> # variable-name

ifdef <fold text='variable-name'>variable-name
<fold text='VAR=value'>VAR = value</fold>
else<fold text=' VAR = value'>
<fold text='VAR=value'>VAR = value</fold>
endif</fold></fold> # variable-name


# ifndef..else..endif
ifndef <fold text='variable-name'>variable-name
# Block body
else<fold text=' ...'>
# Block body
endif</fold></fold> # variable-name

ifndef <fold text='variable-name'>variable-name
<fold text='VAR=value'>VAR = value</fold>
else<fold text=' VAR = value'>
<fold text='VAR=value'>VAR = value</fold>
endif</fold></fold> # variable-name
