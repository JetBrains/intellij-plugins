// "Add variable 'foobar'" "true"
variable "foobar" {
  default = ""
}
output "x" {
  value = "${var.foo<caret>bar}"
}