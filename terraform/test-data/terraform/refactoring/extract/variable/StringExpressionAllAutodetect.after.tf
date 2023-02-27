variable "value" {
  type    = any
  default = "str"
}
output "test" {
  value = "${var.value}"
}
output "second" {
  value = "${var.value}"
}