# intention: "TfUnknownProperty"
# fix: "Remove unknown property"
# position: 4: "non_property = """
#
resource "aws_instance" "aws1" {
  ami           = ""
  instance_type = ""

  ipv6_addresses = []

  ami_mot = ""
}