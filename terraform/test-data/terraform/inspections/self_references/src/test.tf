resource "vault_jwt_auth_backend" "foo" {
  foo = "bar"

  connection {
    host = self.path
  }

  provisioner "shell" {
    value = self.provider

    connection {
      host = self.path
    }
  }
}