# result is list of indices
x = {for i, elem in [3, 2, 1]: i => i}
x = {for i, elem in [true, false, true]: i => i}
x = {for i, elem in []: i => i}
# result is list of elements
x = {for i, elem in [3, 2, 1]: elem => elem}
x = {for i, elem in [true, false, true]: elem => elem}
x = {for i, elem in []: elem => elem}
# with objects
x = {for i, elem in [{}]: elem => elem}

x = {for key, value in {y=1}: key => value}
x = {for key, value in {y=1}: value => key}
