# intention: "TfDuplicatedOutput"
# fix: "Rename block"
# position: 0: "output "a" {"
#
output "newOutput" {
  value = "x"
}
output "b" {
  value = ""
}
output "c" {
  value = ""
}
