# intention: "TfUnknownProperty"
# fix: "Remove unknown property"
# position: 7: "ami_mot = """
#
resource "aws_instance" "aws1" {
  ami           = ""
  instance_type = ""

  non_property = ""
  ipv6_addresses = []

}