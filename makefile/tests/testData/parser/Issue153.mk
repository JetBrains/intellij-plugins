ifeq (${HOSTNAME},Mac.local)
	PG_EXTRA_ARGS =
else
	PG_EXTRA_ARGS := -U test
endif

ifeq ($(HOSTNAME),Mac.local)
	PG_EXTRA_ARGS =
else
	PG_EXTRA_ARGS := -U test
endif