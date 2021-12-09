<fold text='#...' expand='true'># comments
# at

# the
# top.</fold>

name: "Bob"

preferences <fold text='{...}' expand='true'>{
  color: red
  numbers: <fold text='[...]' expand='true'>[1, 2, 3]</fold>
  attributes <fold text='<...>' expand='true'><
    foo: bar
    one: two
  ></fold>
}</fold>
