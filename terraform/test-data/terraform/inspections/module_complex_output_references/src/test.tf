module "sub" {
  source = "./sub"
}

output "resource-from-submodule" {
  value = module.sub.output_resource.id
}

output "output-from-submodule" {
  value = module.sub.output_complex_object.top.middle.bottom
}
