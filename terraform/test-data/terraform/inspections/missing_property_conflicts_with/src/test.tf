resource "aws_instance" "example" {
}

import {
  to = aws_instance.example
}

import {
  to = aws_instance.example
  identity {
  }
}
