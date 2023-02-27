variable "test1" {
  type = list(object({
    name = string
  }))
  default = []
}
variable "test2" {
  type = object({
    name = string
  })
  default = null
}

locals {
  s1 = var.test1[0].name
  s2 = var.test1.0.name
  s3 = var.test2.name
}