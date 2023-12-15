# intention: "HCLBlockMissingProperty"
# fix: "Add properties: source"
# position: 11: "module "test-module" {"
#
provider "docker" {
}
provider "aws" {
}
//noinspection MissingProperty
provider "atlas" {
}
terraform {
  backend "s3" {
  }
}
module "test-module" {
  source = ""
}