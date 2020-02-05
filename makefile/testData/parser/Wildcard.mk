javascript : $(wildcard js/*) FORCE

pathsubst : $(patsubst js/*) FORCE

js/% : public/static/js FORCE
	ugligyjs $@/*.js

FORCE: