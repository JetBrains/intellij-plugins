terraform {
  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "~> 2.38.0"
    }
  }
}

provider "azurerm" {
  features {}
}

resource "azurerm_resource_group" "test" {
  location = "East US"
  name     = "test"
}

resource "azurerm_network_security_group" "nsg" {
  name                = "test_nsg"
  location            = azurerm_resource_group.test.location
  resource_group_name = azurerm_resource_group.test.name
}

resource "azurerm_network_security_rule" "rule" {
  access                      = "Deny"
  direction                   = "Inbound"
  name                        = "TestRule"
  network_security_group_name = azurerm_network_security_group.nsg.name
  priority                    = 1000
  protocol                    = "tcp"
  resource_group_name         = azurerm_resource_group.test.name

  source_port_range      = "*"
  destination_port_range = "*"
  source_address_prefix  = "*"

  # Here's where things go wrong - these will both be highlighted as being errors,
  # even if one of them is marked null and therefore doesn't really 'exist'.
  destination_address_prefix   = "10.0.0.100"
  destination_address_prefixes = null
}