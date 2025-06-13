terraform {
  required_providers {
    azurerm = {
      source  = "hashicorp/azurerm"
      version = "4.32.0"
    }
  }
}
ephemeral "azurerm_key_vault_secret" "test_name" {
  key_vault_id = ""
  name         = ""
}