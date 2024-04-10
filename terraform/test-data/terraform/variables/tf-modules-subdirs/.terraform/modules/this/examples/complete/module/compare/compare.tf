# This module compares the outputs of 2 instances of null-label and determines
# whether or not they are equivalent. Used to detect when changes to new
# versions cause an unintended difference in output/behavior
# that would break compatibility.


variable "a" {
  type = any
}

variable "b" {
  type = any
}

locals {
  equal_id                   = var.a.id == var.b.id
  equal_id_full              = var.a.id_full == var.b.id_full
  equal_tags_as_list_of_maps = jsonencode(var.a.tags_as_list_of_maps) == jsonencode(var.b.tags_as_list_of_maps)
  equal                      = local.equal_id && local.equal_id_full && local.equal_normalized_context && local.equal_tags_as_list_of_maps

  context_keys             = setintersection(keys(var.a.normalized_context), keys(var.b.normalized_context))
  a_context_compare        = {for k in local.context_keys : k => var.a.normalized_context[k]}
  b_context_compare        = {for k in local.context_keys : k => var.b.normalized_context[k]}
  equal_normalized_context = jsonencode(local.a_context_compare) == jsonencode(local.b_context_compare)

}

output "equal" {
  value = local.equal
}

output "equal_id" {
  value = local.equal_id
}

output "equal_id_full" {
  value = local.equal_id_full
}

output "equal_normalized_context" {
  value = local.equal_normalized_context
}

output "equal_tags_as_list_of_maps" {
  value = local.equal_tags_as_list_of_maps
}


