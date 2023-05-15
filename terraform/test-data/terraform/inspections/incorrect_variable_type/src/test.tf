variable "stringButMap" {
  type = "string"
  default = {
    x = 42
  }
}
variable "stringButList" {
  type = "string"
  default = [
    42
  ]
}
variable "mapButString" {
  type = "map"
  default = "42"
}
variable "mapButList" {
  type = "map"
  default = [
    42
  ]
}
variable "listButString" {
  type = "list"
  default = "42"
}
variable "listButMap" {
  type = "list"
  default = {
    x = 42
  }
}
variable "weird" {
  type = "weird"
}


variable "tf_0.12" {
  type    = string
  default = null # Must be no error in 0.12
}

variable "infer-map" {
  default = {
    a = "value-a"
  }
}
variable "infer-list" {
  default = [
    "list1",
    "list2",
  ]
}

# from 272, type match
variable "ip_map_1" {
  type    = map
  default = {
    "Test" = "8.8.8.8"
  }
}
# from 272, type match
variable "ip_map_2" {
  type    = map(string)
  default = {
    "Test" = "8.8.8.8"
  }
}
# from 272, type mismatch, but not reported since we don't use value for conversion
variable "ip_map_3" {
  type    = map(number)
  default = {
    "Test" = "8.8.8.8"
  }
}

# from 338, 'optional'
variable "optional" {
  type = object({
    a = string           # a required attribute
    b = optional(string) # an optional attribute
  })
  default = {
    a = "a"
  }
}

# from 362
variable "object-to-map-of-strings" {
  default     = {}
  type        = map(string)
}

# from 338, 'optional'
variable "optional" {
  type = object({
    a = string           # a required attribute
    b = optional(string) # an optional attribute
  })
  default = {
    a = "a"
  }
}