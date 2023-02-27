resource "null_resource" "test1" {
  for_each = {
    "key" = "value"
  }
  test_key = "${each.key}"
  test_value = "${each.value}"
  test_else = "${random.value}"
}
module "test1" {
  for_each = {
    "key" = "value"
  }
  test_key = "${each.key}"
  test_value = "${each.value}"
  test_else = "${random.value}"
}
