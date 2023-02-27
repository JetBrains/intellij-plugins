variable "objs" {
  type = "list"
  default = [
    {
      name = "a"
      number = 1
    },
    {
      name = "b"
      number = 2
    }
  ]
}

output "o_for_var" {
  value = {
    for o in var.objs:
      o.name => o.number
  }
}

for_each = [for s in var.objs: {
  name = s.name
  prefix = s.number
}]