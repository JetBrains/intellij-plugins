variable "y" {
  default = ""
}
variable "x" {
  default = ""
}
resource "null_resource" "test" {
  testIL = "${var.x}"
  testEX = var.y
}


