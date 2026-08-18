all:
	echo "This is highlighted fine"
	
VAR1 := 1
VAR2 := 2

$(eval)

# This assignment is shown as error
VAR3 := 3
# And there is no syntax highlighting afterwards
VAR4 := 4