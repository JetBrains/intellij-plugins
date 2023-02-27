variable "map_obj_set_partial" {
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

data "local_file" "map_obj_set_partial" {
  for_each = var.map_obj_set_partial
  filename = each.value.service
}