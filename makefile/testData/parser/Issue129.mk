ifeq ($(origin VAR1)-$(origin VAR2),undefined-undefined)
 #...
endif

ifeq (undefined-undefined,$(origin VAR1)-$(origin VAR2))
 #...
endif
ifeq (undefinedundefined,$(origin VAR1)$(origin VAR2))
endif