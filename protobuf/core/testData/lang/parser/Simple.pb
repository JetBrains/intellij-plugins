# Some
# Comments

name: "Bob"

value: true

message1 <
  sub_message {
    foo: bar
  }
  empty: <>

  ends_with_comma: true,
  ends_with_semi: true;
>

message2: {
  repeated1: foo
  repeated1: bar
  repeated2: [1, 2, 3]

  repeated_with_messages: [
    {
      foo: bar
    },
    { foo: baz },
    { foo: rocket }
  ]
}

[com.foo.bar.int_value]: 10
[com.foo.bar.message_value] {
  color: red
}

any_value {
  [type.googleapis.com/proto2_unittest.TestAllTypes] {
    optional_int32: 321
    optional_string: "teststr0"
  }
}
