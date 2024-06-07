//noinspection MissingProperty
resource "azurerm_linux_virtual_machine_scale_set" "example" {
  name                 = "example-vmss"
  sku                  = "Standard_F2"
  instances            = 4
  admin_username       = "adminuser"
  admin_password       = "P@ssword1234!"
  computer_name_prefix = "my-linux-computer-name-prefix"
  upgrade_mode         = "Automatic"

  disable_password_authentication = false

  source_image_reference {
    publisher = "Canonical"
    offer     = "0001-com-ubuntu-server-jammy"
    sku       = "22_04-lts"
    version   = "latest"
  }

  os_disk {
    storage_account_type = "Standard_LRS"
    caching              = "ReadWrite"
  }

  network_interface {
    name    = "example"
    primary = true

    ip_configuration {
      name      = "internal"
      primary   = true
      subnet_id = "127"
    }
  }
}