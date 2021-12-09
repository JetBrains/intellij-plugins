bar:
	if [ -f foo ]; then \
	  echo "##hello, sir!"; \
	fi

target:
	ls
ifeq ($(A),b)
	# here be the error
	ls
endif
	ls
