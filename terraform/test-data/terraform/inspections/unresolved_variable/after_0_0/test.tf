variable "y" {
  default = ""
}
resource "null_resource" "test" {
  testIL = "${var.x}"
  testEX = var.y
}


