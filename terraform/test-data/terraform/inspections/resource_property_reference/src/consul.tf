data "consul_keys" "demo" {
}
data "consul_keys" "demo2" {
  key {
    name = "example"
  }
}
resource "archive_file" "init" {
  source_content = "${data.consul_keys.demo.var.example}"
}
resource "archive_file" "init" {
  source_content = "${data.consul_keys.demo2.var.example}"
}