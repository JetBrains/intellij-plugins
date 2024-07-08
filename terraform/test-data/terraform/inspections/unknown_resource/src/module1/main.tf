terraform {
  required_version = ">= 0.12.26"
}

data "aws_availability_zones" "all" {}


resource "aws_instance" "resource_local_name1" {
  ami           = var.image_id
  instance_type = var.instance_type
}

resource "aws_instance" "resource-name-test1" {
  ami           = var.image_id
  instance_type = var.instance_type
}
