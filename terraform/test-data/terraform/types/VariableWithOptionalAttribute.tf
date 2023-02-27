#stop
variable "v3" {
  type = object({
    a = string           # a required attribute
    b = optional(string) # an optional attribute
  })
}

locals {
#start
  l1 = var.v3
  l2 = var.v3.a
  l3 = var.v3.b
}