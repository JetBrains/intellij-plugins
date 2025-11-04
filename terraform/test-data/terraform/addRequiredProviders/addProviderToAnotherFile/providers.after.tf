terraform {
  required_providers {
    aws = {
      source  = "hashicorp/aws"
      version = "6.17.0"
    }
    alicloud = {
      source  = "aliyun/alicloud"
      version = "1.261.0"
    }
  }
}