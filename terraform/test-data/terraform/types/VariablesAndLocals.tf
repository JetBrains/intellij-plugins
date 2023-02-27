#stop
variable v1 {
  type = list(number)
}

locals {
#start
  l1 = var.v1
  l2 = var.v1[0]
}