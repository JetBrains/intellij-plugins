locals {
  objs = [
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

output "o_for_locals" {
  value = {
    for o in local.objs:
      o.name => o.number
  }
}

for_each = [for s in local.objs: {
  name = s.name
  prefix = s.number
}]

for_each_indexed = [for index, s in local.objs: {
  name = substr(s.name, index)
  prefix = s.number
}]