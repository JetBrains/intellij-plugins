resource "aws_secretsmanager_secret" "test" {}

module "lol" {
  source = ""

  depends_on = [aws_secretsmanager_secret.test]
}