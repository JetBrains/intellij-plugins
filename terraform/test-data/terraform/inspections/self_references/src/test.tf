resource "aws_instance" "foo" {
  foo = "bar"

  connection {
    host = self.public_ip
  }

  provisioner "shell" {
    value = self.public_ip

    connection {
      host = self.public_ip
    }
  }
}
