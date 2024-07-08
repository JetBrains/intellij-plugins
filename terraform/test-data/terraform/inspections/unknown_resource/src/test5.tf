module "test-module1-name" {
  source        = "module1"
  image_id      = "ami-a1b2c3d4"
  instance_type = "m5.large"
}

variable "test_id" {
  type          = string
}

output "test_output" {
  value = "test"
}

