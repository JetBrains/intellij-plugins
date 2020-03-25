reverse = $(2) $(1)

foo = $(call reverse,a,b)

pathsearch = $(firstword $(wildcard $(addsuffix /$(1),$(subst :, ,$(PATH)))))

LS := $(call pathsearch,ls)

map = $(foreach a,$(2),$(call $(1),$(a)))

o = $(call map,origin,o map MAKE)