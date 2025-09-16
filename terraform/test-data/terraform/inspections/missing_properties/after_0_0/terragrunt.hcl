# intention: "HclBlockMissingProperty"
# fix: "Add missing properties"
# position: 0: "remote_state"
#
remote_state {
  generate = {
    path      = "backend.tf"
    if_exists = "overwrite"
  }
  config = {
    bucket         = "my-terraform-state"
    key            = "${path_relative_to_include()}/terraform.tfstate"
    region         = "us-east-1"
    encrypt        = true
    dynamodb_table = "my-lock-table"
  }
  backend = ""
}