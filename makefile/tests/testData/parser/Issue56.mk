SHELL := /bin/bash

ifneq ("$(wildcard foo.mk)","")
else ifneq ("$(wildcard bar.mk)","")
endif