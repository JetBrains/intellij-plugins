javascript : $(wildcard js/*) FORCE

patsubst : $(patsubst js/*) FORCE

js/% : public/static/js FORCE
	ugligyjs $@/*.js

FORCE: