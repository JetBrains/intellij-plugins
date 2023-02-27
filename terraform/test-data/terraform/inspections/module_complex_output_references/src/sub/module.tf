resource "null_resource" "test" {}
output "output_resource" {
  value = null_resource.test
}

output "output_complex_object" {
  value = {
    top = {
      middle = {
        bottom = "nope"
      }
    }
  }
}
