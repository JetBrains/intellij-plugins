# intention: "TfDuplicatedVariable"
# fix: "Rename block"
# position: 0: "variable "x" {"
#
variable "newVar" {
}
variable "y" {
  default = "1"
}
variable "z" {
}
