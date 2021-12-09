# This set VARIABLE to "foo" for the duration of handling test-bar
test-foo: export VARIABLE=foo
# The above target are not actually a executable target. Without the next line you would get.
# make: *** No rule to make target 'test-foo', needed by 'all'.  Stop.
test-foo:
	@echo "$$VARIABLE"

# This set VARIABLE to "bar" for the duration of handling test-bar
test-bar: export VARIABLE=bar
test-bar:
	@echo "$$VARIABLE"

all: test-foo test-bar