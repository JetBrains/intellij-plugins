S=target

$(S):
	@ echo "make the target"

$(S:=-clean):
	@ echo "clean the target"