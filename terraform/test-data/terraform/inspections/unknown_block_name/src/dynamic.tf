//noinspection MissingProperty
resource "azurerm_orchestrated_virtual_machine_scale_set" "example" {
  name                = "example-VMSS"
  location            = "West Europe"
  resource_group_name = "example-rg"
  dynamic "network_interface" {
    for_each = ""
    content {
      name = "if1"
      dynamic "ip_configuration" {
        for_each = ""
        content {
          name = "conf1"
        }
      }
    }
  }
}

resource "azurerm_network_interface" "example" {
  name                = "example-nic"
  location            = "West Europe"
  resource_group_name = "example-rg"

  dynamic "ip_configuration" {
    for_each = local.overrides
    content {
      name                          = "internal"
      subnet_id                     = "example-nic"
      private_ip_address_allocation = "Dynamic"
    }
  }
}