projects = someProject1 someProject2

push-all: $(addprefix push-,$(projects))

all: build-all push-all