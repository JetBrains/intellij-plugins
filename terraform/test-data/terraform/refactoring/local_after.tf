locals {
  new_local = "some_value"
}

resource "aws_instance" "example" {
  tags = {
    Name = local.new_local
  }
}