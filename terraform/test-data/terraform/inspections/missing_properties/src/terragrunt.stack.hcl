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
}

stack "dev" {
  path = "dev"
  values = {
    environment = "development"
    cidr        = "10.0.0.0/16"
  }
}