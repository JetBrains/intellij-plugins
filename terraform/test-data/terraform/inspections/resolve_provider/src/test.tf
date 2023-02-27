provider "aws" {
  region = "eu-west-1"
}
provider "aws" {
  alias  = "us"
  region = "us-east-1"
}

resource "aws_iam_user" "us" {
  name     = "us"
  provider = aws.us
}
resource "aws_iam_user" "eu" {
  name     = "eu"
  provider = aws
}

module "test" {
  source = "./sub"
  providers = {
    aws = aws.us
    aws.target = aws
  }
}
