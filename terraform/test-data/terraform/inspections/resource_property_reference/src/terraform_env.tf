output "test_terraform_env" {
  value = "${terraform.env == "default" ? 5 : 1}"

}