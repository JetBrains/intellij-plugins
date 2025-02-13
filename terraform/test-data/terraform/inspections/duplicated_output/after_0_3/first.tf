# intention: "TfDuplicatedOutput"
# fix: "Rename output"
# position: 0: "output "a" {"
#
output "a" {
  value = "x"
}
output "b" {
  value = ""
}
output "c" {
  value = ""
}
