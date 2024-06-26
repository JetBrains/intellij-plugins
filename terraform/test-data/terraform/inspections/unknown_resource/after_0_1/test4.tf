# intention: "TfUnknownResource"
# fix: "Add provider to required providers"
# position: 0: "provider "tsuru" {"
#
terraform {
  required_providers {
    tsuru = {
      source  = "tsuru/tsuru"
      version = "2.12.0"
    }
  }
}
provider "tsuru" {

}
