// "Add variable 'foobar'" "true"
output "x" {
  value = "${var.foo<caret>bar}"
}