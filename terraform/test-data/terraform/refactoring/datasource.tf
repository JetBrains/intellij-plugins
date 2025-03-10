data "aws_iam_role" "<caret>old_role" {
  name = "my-role"
}

resource "aws_lambda_function" "lambda" {
  role = data.aws_iam_role.old_role.arn
}