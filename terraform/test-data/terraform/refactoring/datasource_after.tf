data "aws_iam_role" "new_role" {
  name = "my-role"
}

resource "aws_lambda_function" "lambda" {
  role = data.aws_iam_role.new_role.arn
}