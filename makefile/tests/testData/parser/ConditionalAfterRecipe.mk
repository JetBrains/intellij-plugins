install-common:
#	@echo install

ifneq $(CONDITION)
install: install-common
	@echo install1
else
install: install-common
	@echo install2
endif