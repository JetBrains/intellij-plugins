# intention: "HILUnresolvedReference"
# fix: "Add variable 'y'"
# position: 2: "y"
#
variable "y" {
  default = ""
}
resource "null_resource" "test" {
  testIL = "${var.x}"
  testEX = var.y
}
