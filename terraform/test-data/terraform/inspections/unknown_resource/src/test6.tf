terraform {
  required_providers {
    aws-sso-scim2 = {
      source  = "burdaforward/aws-sso-scim"
      version = ">= 0.1.0"
    }
  }
}

resource "aws-sso-scim2_user" "user" {
  display_name = "John Doe"
  family_name  = "Doe"
  given_name   = "John"
  user_name    = "jdoe"
}
