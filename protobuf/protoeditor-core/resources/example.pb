# This is an example of Protocol Buffer's text format.
# Unlike .proto files, only sh-style line comments are supported.

name: "John Smith"

pet {
  kind: DOG
  name: "Fluffy"
  tail_wagginess: 0.65f
}

pet <
  kind: LIZARD
  name: "Lizzy"
  legs: 4
>

string_value: "valid \n escape"
string_value: "invalid \Uabc escape"

repeated_values: [ "one", "two", "three" ]
