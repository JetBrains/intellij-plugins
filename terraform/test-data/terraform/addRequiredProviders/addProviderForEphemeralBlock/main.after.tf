terraform {
  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "5.9.0"
    }
  }
}
ephemeral "azurerm_key_vault_secret" "test_name" {
  key_vault_id = ""
  name         = ""
}