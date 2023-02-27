foo = "9000"
empty_objects_are_ok = {}
baz = {
  a = "1"
  "b" = "2"
  c = true
}
//baz.a = "1"
//baz.b = "2"
//baz.c = "3"
//amis.us-east-1 = "foo"
//amis.us-west-2 = "baz"
blocks are not expected here {
}
"string" = []
"true" = false
list = [1,2,3]
list = "string"
list = {
  map = true
}
"string" = 42
string = false

network = {
  subnets = ["a","v"]
}