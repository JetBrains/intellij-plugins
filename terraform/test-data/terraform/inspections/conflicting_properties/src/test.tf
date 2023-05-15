resource "aws_key_pair" "test" {
  public_key = ""
  key_name = ""
  key_name_prefix = ""
}

variable "list" {
  default = [1, 2, 3, 4, 5]
}

resource "local_file" "rfile" {
  for_each = var.list
  count = 5
}

data "local_file" "dfile" {
  for_each = var.list
  count = 5
}