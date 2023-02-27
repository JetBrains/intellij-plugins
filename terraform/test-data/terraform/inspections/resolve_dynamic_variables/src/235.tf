variable "list" {
  default = [1, 2, 3, 4, 5]
}

resource "local_file" "rfile" {
  for_each = zipmap(var.list, var.list)
  filename = "/tmp/${each.key}-${each.value}"
}

data "local_file" "dfile" {
  for_each = { "a" : "b" }
  filename = each.key
  content  = each.value
}

locals {
  fleets = { "a" : "b" }
  instance_types_map = [
    { i_type : "c5.large" },
    { i_type : "m5.large" },
  ]
}
//noinspection MissingProperty
resource "aws_ec2_fleet" "fleet2" {
  for_each = local.fleets
  //noinspection MissingProperty
  launch_template_config {
    dynamic "override" {
      for_each = local.instance_types_map
      iterator = each
      content {
        instance_type = each.value.i_type
        availability_zone = "${each.value.i_type}"
      }
    }
  }
  target_capacity_specification {
    default_target_capacity_type = "spot"
    total_target_capacity        = each.value
  }
}

resource "local_file" "without_foreach" {
  filename = each.value
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

data "local_file" "dvfile" {
  for_each = var.map_obj_set
  filename = each.value.service
}