# ifeq..endif
ifeq <fold text='(arg1, arg2)'>(arg1, arg2)
# Block body
endif</fold>

ifeq <fold text='"arg1" "arg2"'>"arg1" "arg2"
# Block body
endif</fold>


# ifneq..endif
ifneq <fold text='(arg1, arg2)'>(arg1, arg2)
# Block body
endif</fold>

ifneq <fold text='"arg1" "arg2"'>"arg1" "arg2"
# Block body
endif</fold>


# ifdef..endif
ifdef <fold text='variable-name'>variable-name
# Block body
endif</fold> # variable-name


# ifndef..endif
ifndef <fold text='variable-name'>variable-name
# Block body
endif</fold> # variable-name
