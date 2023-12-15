# intention: "HILUnresolvedReference"
# fix: "Add variable 'x'"
# position: 1: "x"
#
variable "x" {
  default = ""
}
resource "null_resource" "test" {
  testIL = "${var.x}"
  testEX = var.y
}
