resource "vault_mount" "kvv2-example" {
  path        = "version2-example"
  type        = "kv-v2"
  options = {
    version = "2"
    type    = "kv-v2"
  }
  abracadabra {
  }
  description = "This is an example KV Version 2 secret engine mount"
}

terraform {
  required_version = "> 0.8.0"
  experiments = [variable_validation]
  required_providers {
    local = "~> 1.2"
    null = "~> 2.1"
  }
}

moved {
  from = test1 
  to   = test2
}

terraform {
  cloud {
    organization = ""
    workspaces {
      name = "ddd"
    }
  }
}
