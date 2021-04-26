# These cases are valid, since a space exists between the number and the field name.
num: 100 num: 100
num: 100f num:100f

# These are also valid. A space is only required between a number and an identifier.
str: "foo"str: "foo"
[foo.bar]: 100[foo.bar]: 100

# Shouldn't affect comments, either.
# num: 100num: 100

# These cases are not valid and should generate error elements.
num: 100num: 100
num: 100fnum:100f
num: 1.0num:1.0
num: 1.0fnum:1.0f
