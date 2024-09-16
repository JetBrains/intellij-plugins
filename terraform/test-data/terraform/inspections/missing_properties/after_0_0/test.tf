# intention: "HCLBlockMissingProperty"
# fix: "Add missing properties"
# position: 10: ""vault""
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
  address = ""
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
