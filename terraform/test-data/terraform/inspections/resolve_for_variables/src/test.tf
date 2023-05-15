resource "null_resource" "example" {
}

output "o" {
  value = {
    for instance in null_resource.example:
      instance.id => instance.count
  }
}
output "o2" {
  value = [for s in null_resource.example: {
    name = s.id
    prefix = s.count
  }]
}