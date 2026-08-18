FOO = world

a:
	@echo $(<caret>FOO)
	@echo $(FOO)
