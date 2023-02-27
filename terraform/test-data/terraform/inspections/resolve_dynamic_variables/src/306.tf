module "mfile" {
  for_each = { "a" : "b" }
  filename = each.key
  content  = each.value
}
