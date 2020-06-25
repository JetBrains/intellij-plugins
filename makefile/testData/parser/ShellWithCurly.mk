# check for executables
PYFLAKES = $(shell { command -v pyflakes-3 || command -v pyflakes3 || command -v pyflakes; }  2> /dev/null)
PYCODESTYLE := $(shell { command -v pycodestyle-3 || command -v pycodestyle3 || command -v pycodestyle; } 2> /dev/null)