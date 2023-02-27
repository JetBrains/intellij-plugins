locals {
  map_obj = {
    x = {
      port    = 1
      service = "test"
    }
    y = {
      port    = 2
      service = "test"
    }
  }
  list = [1, 2, 3]
}

variable "map_obj_unset" {
  type = map(object({
    port    = number
    service = string
  }))
}

variable "map_obj_set" {
  type = map(object({
    port    = number
    service = string
  }))
  default = {
    x = {
      port    = 1
      service = "test"
    }
    y = {
      port    = 2
      service = "test"
    }
  }
}

locals {
  local_port_l   = [for row in local.map_obj : row.service]
  local_port_o   = { for row in local.map_obj : row.port => row.service }
  local_x        = { for row in local.map_obj : row.x => row.y }
  var_set_port   = { for row in var.map_obj_set : row.port => row.service }
  var_set_x      = { for row in var.map_obj_set : row.x => row.y }
  var_unset_port = { for row in var.map_obj_unset : row.port => row.service }
  var_unset_x    = { for row in var.map_obj_unset : row.x => row.y }
  local_list_o   = { for s in local.list : s => upper(s) }
  local_list_l   = [for s in local.list : upper(s) if s != 0]
}

output "lpo" {
  value = local.local_port_o
}
output "lpl" {
  value = local.local_port_l
}
output "vsp" {
  value = local.var_set_port
}
output "vup" {
  value = local.var_unset_port
}

output "llo" {
  value = local.local_list_o
}
output "lll" {
  value = local.local_list_l
}