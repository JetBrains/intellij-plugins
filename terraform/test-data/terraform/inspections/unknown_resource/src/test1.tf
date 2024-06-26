provider "vault" {
  address = "http://vault:8200/"
}

data "vault_identity_group" "group" {
  group_name = "tf-auto-vault-group-tf"
}