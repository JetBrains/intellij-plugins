# Terraform Configuration for a Virtual Machine on Azure
# This Terraform configuration creates a basic Azure Virtual Machine.

# Specify the Azure provider and set the default region
provider <caret>"azurerm" {
features = {}
}

# Declare an Azure Virtual Network
resource "azurerm_virtual_network" "example_vnet" {
name = "my-example-vnet"
address_space = ["10.0.0.0/16"]
location = ""
resource_group_name = ""
}