variable "foo" {
  type    = any
  default = "str"
}
output "test" {
  value = "${var.foo}"
}