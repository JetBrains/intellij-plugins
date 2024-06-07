# intention: "HCLBlockMissingProperty"
# fix: "Add properties: source"
# position: 19: "module "test-module" {"
#
terraform {
  required_providers {
    docker = {
      source = "kreuzwerker/docker"
      version = ">= 0.0.1"
    }
  }
}
provider "docker" {
}
provider "vault" {
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
