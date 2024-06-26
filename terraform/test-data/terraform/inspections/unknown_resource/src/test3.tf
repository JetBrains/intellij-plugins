terraform {
  required_version = "1.1.3"
}
resource "abbey_demo" "demo" {
  email      = "a@a.a"
  permission = "user"
}