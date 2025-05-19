# intention: "TfBlockNameValidness"
# fix: "Rename"
# position: 13: """"
#
terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "5.81.0"
    }
  }
}

provider "aws" {
  region = "us-east-1"
}

resource "aws_s3_bucket" "new_name" {
  bucket = "my-unique-example-bucket-name-12345"
}
