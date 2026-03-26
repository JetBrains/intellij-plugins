# intention: "HclBlockMissingProperty"
# fix: "Add missing properties"
# position: 3: "import"
#
resource "aws_instance" "example" {
}

import {
  to = aws_instance.example
  id = ""
}

import {
  to = aws_instance.example
  identity {
  }
}
