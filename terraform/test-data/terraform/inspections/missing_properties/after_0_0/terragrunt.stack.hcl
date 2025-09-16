# intention: "HclBlockMissingProperty"
# fix: "Add missing properties"
# position: 9: ""database""
#
unit "vpc" {
  source = "git::git@github.com:acme/infrastructure-catalog.git//units/vpc?ref=v0.0.1"
  path   = "vpc"
  values = {
    vpc_name = "main"
    cidr     = "10.0.0.0/16"
  }
}

unit "database" {
  values = {
    engine  = "postgres"
    version = "13"

    vpc_path = "../vpc"
  }
  path   = ""
  source = ""
}

stack "dev" {
  path = "dev"
  values = {
    environment = "development"
    cidr        = "10.0.0.0/16"
  }
}