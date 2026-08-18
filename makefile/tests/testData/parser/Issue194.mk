test:
ifeq ($(A):$(B),1:2)
	echo yes
endif
	echo no