terraform {}

resource "aws-sso-scim2_user" "user" {
  display_name = "John Doe"
  family_name  = "Doe"
  given_name   = "John"
  user_name    = "jdoe"
}
