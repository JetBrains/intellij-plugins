# intention: "TfDuplicatedVariable"
# fix: "Rename variable"
# position: 0: "variable "x" {"
#
variable "newVar" {
}
variable "y" {
  default = "1"
}
variable "z" {
}
