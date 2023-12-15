# intention: "HCLBlockMissingProperty"
# fix: "Add properties: region"
# position: 2: "provider "aws" {"
#
provider "docker" {
}
provider "aws" {
  region = ""
}
//noinspection MissingProperty
provider "atlas" {
}
terraform {
  backend "s3" {
  }
}
module "test-module" {
}
