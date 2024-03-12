provider "azurerm" {
  features {}
}

<selection>resource "azurerm_kubernetes_cluster" "dso_aks_cluster" {
  name                = "example-aks1"
  location            = azurerm_resource_group.example.location
  resource_group_name = azurerm_resource_group.example.name
  dns_prefix          = "exampleaks1"


  default_node_pool {
    identity {
      type = "SystemAssigned"
    }
    name       = "default"
    node_count = 1

    tags = {
      Environment = "Production"
    }
    vm_size    = "Standard_DS2_v2"
  }

}

resource "azurerm_resource_group" "example" {
  location = "East US"
  name     = "example-resources"
}</selection>

output "kubelet_identity_object" {
  value       = azurerm_kubernetes_cluster.dso_aks_cluster.kubelet_identity.0
  description = "Specifies the object ID of the kubelet identity"
}
