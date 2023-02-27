#stop
variable "map_obj_unset" {
  type = map(object({
    port    = number
    service = string
  }))
}
variable "map_obj_set_partially" {
  type = map(object({
    port    = number
    service = string
  }))
  // TF Error:
  //  This default value is not compatible with the variable's type constraint:
  //  element "x": attribute "service" is required.
  default = {
    x = {
      port = 1
    }
    y = {
      port = 2
    }
  }
}
variable "map_obj_set_partially_list" {
  type = list(map(object({
    port    = number
    service = string
  })))
  // TF Error:
  //  This default value is not compatible with the variable's type constraint:
  //  element "x": attribute "service" is required.
  default = [{
    x = {
      port = 1
    }
    y = {
      port = 2
    }
  }]
}

locals {
  #start
  var_unset = var.map_obj_unset
  var_set_partially = var.map_obj_set_partially
  var_set_partially_list = var.map_obj_set_partially_list
  var_set_partially_port = {
    for row in var.map_obj_set_partially_list[0] :
    row.port => row.service
  }
  var_set_partially_service = {
    for row in var.map_obj_set_partially_list[0] :
    row.service => row.port
  }
}