NEW_NAME := one
BAR ?= $(NEW_NAME)
BAZ += $(NEW_NAME)

all: $(NEW_NAME) $(BAR) $(BAZ)
