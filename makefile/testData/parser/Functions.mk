ifdef TEST
  $(error Some error)
  $(warning Some warning)
  $(info test is $(TEST))
  CC=$(shell which gcc)
  $(firstword $(wildcard $(addsuffix /$(1), $(subst :, , $(PATH)))))
endif