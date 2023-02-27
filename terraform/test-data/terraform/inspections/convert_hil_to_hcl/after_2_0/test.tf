variable "foo" {}
variable "bar" {}
output "3" { value = var.foo }
output "4" { value = "${var.foo} " }
output "5" { value = "${var.foo}${var.bar}" }
output "6" { value = "${var.foo} ${var.bar}" }
output "7" { value = "${var."${nope}"}" }
output "8" { value = "${var.${nope}}" }
output "9" { value = "${${nope}}" }
output "10" { value = "prefix" }
output "11" { value = "${"prefix"+var.foo}" }
output "12" { value = "${("prefix")+var.foo}" }
tags = merge({ (var.foo): "bar" })