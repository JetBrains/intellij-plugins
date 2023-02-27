# result is list of indices
x = [for i, elem in [3, 2, 1]: i]
x = [for i, elem in [true, false, true]: i]
x = [for i, elem in []: i]
# result is list of elements
x = [for i, elem in [3, 2, 1]: elem]
x = [for i, elem in [true, false, true]: elem]
x = [for i, elem in []: elem]
# with objects
x = [for i, elem in [{}]: elem]

x = [for key, value in {y=1}: key]
x = [for key, value in {y=1}: value]