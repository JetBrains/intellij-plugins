data "external" "ext" {
  program = ["echo", "{ \"a\" : \"lol\" }"]
}
output "from_external" {
  value = "${data.external.ext.result.a}"
}