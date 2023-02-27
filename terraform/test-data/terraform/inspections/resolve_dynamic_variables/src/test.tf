variable "objs" {
  type = "list"
  default = [
    {
      name   = "a"
      number = 1
    },
    {
      name   = "b"
      number = 2
    }
  ]
}
variable "objs_ed" {
  type = list(object({
    name   = string,
    number = number
  }))
  default = []
}

locals {
  base_cidr_block = ""
}

resource "azurerm_virtual_network" "example" {
  dynamic "subnet" {
    for_each = [for aa in var.objs : {
      name   = aa.name
      prefix = cidrsubnet(local.base_cidr_block, 4, aa.number)
    }]

    content {
      name           = subnet.value.name
      address_prefix = subnet.value.prefix
    }
  }
  dynamic "subnet" {
    for_each = var.objs
    iterator = xxx
    labels   = [xxx.key]
    content {
      name           = xxx.key
      address_prefix = cidrsubnet(local.base_cidr_block, 4, xxx.value.number)
    }
  }
  dynamic "subnet" {
    for_each = var.objs_ed
    iterator = xxx
    labels   = [xxx.key]
    content {
      name           = xxx.value.name
      address_prefix = cidrsubnet(local.base_cidr_block, 4, xxx.value.number)
    }
  }

  address_space       = []
  location            = ""
  name                = ""
  resource_group_name = ""
}
