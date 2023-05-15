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

locals {
  var_set_partially_port = { for row in var.map_obj_set_partially : row.port => row.service }
  var_set_partially_x    = { for row in var.map_obj_set_partially : row.x => row.y }
}