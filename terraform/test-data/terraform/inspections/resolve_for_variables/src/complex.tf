locals {
  users_list = [
    {
      username = "root"
    }
  ]
  users_names = [
    for index, user in local.users_list : user.username
  ]
  users_map = {
    for index, user in local.users_list :
    user.username => list(index,
      random_string.random_password[index].result
    )
  }
}
variable "users_list" {
  type = "list"
  default = [
    {
      username = "a"
    }
  ]
}

resource "random_string" "random_password" {
  length           = 16
  special          = true
  override_special = "/@\" "
  count            = length(local.users_list)
}
