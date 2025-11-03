# There can be multiple terraform blocks within a single module directory,
# but only one required_providers block is allowed per module
terraform {
  required_providers {
    datadog = {
      source  = "Datadog/Datadog"
      version = "3.42.0"
    }
    dockerk = {
      source  = "calXus/doCKer"
      version = "3.0.0"
    }
    aws-sso-scim = {
      source  = "burdaforward/aws-sso-scim"
      version = ">= 0.1.0"
    }
    vkcs = {
      source  = "vk-cs/vkcs"
      version = "0.1.11"
    }
    zabbix = {
      source  = "ElastiC-iNfRa/Zabbix"
      version = "1.1.3"
    }
  }
}

resource "aws-sso-scim_user" "user" {
  display_name = "John Doe"
  family_name  = "Doe"
  given_name   = "John"
  user_name    = "jdoe"
}

resource "ably_app" "ably" {
  name = "app"
}