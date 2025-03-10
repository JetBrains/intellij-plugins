locals {
  old_local<caret> = "some_value"
}

resource "aws_instance" "example" {
  tags = {
    Name = local.old_local
  }
}