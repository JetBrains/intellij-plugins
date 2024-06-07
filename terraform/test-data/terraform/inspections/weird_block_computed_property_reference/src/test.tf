resource "vault_jwt_auth_backend" "gsuite" {
  description = "OIDC backend"
  oidc_discovery_url = "https://accounts.google.com"
  path = "oidc"
  type = "oidc"
  provider_config = {
    provider = "gsuite"
    fetch_groups = true
    fetch_user_info = true
    groups_recurse_max_depth = 1
    tags = {
      gsuite = {
        jwt_token = "${TOKEN}"
        username = "test"
      }
    }
  }
}

resource "vault_jwt_auth_backend" "gsuite2" {
  description = "OIDC backend"
  oidc_discovery_url = "https://accounts.google.com"
  path = "oidc"
  type = "oidc"
  provider_config = {
    provider = "gsuite"
    fetch_groups = true
    fetch_user_info = true
    groups_recurse_max_depth = 1
    tags = {
      account = {
        jwt_token = "${TOKEN}"
        username = "test"
      }
    }
  }
}


resource "null_resource" "test" {
  connection {
    backend1 = "${vault_jwt_auth_backend.gsuite.provider_config.tags.0.gsuite.0.jwt_token}"
    backend1 = "${vault_jwt_auth_backend.gsuite2.provider_config.tags.0.account.0.jwt_token}"
  }
}
