variable "foo" {
  type    = string
  default = "str"
}
output "test" {
  value = "${var.foo}"
}