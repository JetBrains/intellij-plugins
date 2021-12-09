$(foreach var,list,text)

dirs := a b c d
files := $(foreach dir,$(dirs),$(wildcard $(dir)/*))

files := $(wildcard a/* b/* c/* d/*)

find_files = $(wildcard $(dir)/*)
dirs := a b c d
files := $(foreach dir,$(dirs),$(find_files))