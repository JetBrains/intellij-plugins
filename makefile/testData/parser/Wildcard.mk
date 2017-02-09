javascript : $(wildcard js/*) FORCE

js/% : public/static/js FORCE
	ugligyjs $@/*.js

FORCE: