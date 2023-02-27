variable "foo" {
  default = "42"
  type = "string"
}
variable "baz" {
  type = "map"
}
variable "amis" {
  default = {
  }
}
variable "empty_objects_are_ok" {
  type = "map"
}
variable "string" {
}
variable "list" {
  type = "list"
}
variable "network" {
  type = object({
    subnets = list(string)
  })
}