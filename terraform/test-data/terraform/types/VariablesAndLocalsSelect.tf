#stop
locals {
  workspace = {
    production = {
      dns_name = "some_name"
    }
  }
}

locals {
  #start
  current  = local.workspace["production"].dns_name
  current2 = local.workspace.production.dns_name
}
output "current" {
  value = local.current
}
output "current2" {
  value = local.current2
}