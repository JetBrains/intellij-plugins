# intention: "TfUnknownResource"
# fix: "Add provider to required providers"
# position: 3: "resource "abbey_demo" "demo" {"
#
terraform {
  required_version = "1.1.3"
  required_providers {
    abbey = {
      source  = "abbeylabs/abbey"
      version = "0.2.9"
    }
  }
}
resource "abbey_demo" "demo" {
  email      = "a@a.a"
  permission = "user"
}
