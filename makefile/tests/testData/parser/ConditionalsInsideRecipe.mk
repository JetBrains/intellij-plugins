clean:
	echo hello
ifeq ($(BUILD_TYPE),QA)
	echo world
else
	rm -rf dependencies
endif
	echo end