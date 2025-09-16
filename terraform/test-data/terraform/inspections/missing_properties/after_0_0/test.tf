# intention: "HclBlockMissingProperty"
# fix: "Add missing properties"
# position: 19: ""test-module""
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
