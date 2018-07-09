javascript : $(wildcard js/*) FORCE

pathsubst : $(pathsubst js/*) FORCE

js/% : public/static/js FORCE
	ugligyjs $@/*.js

FORCE: