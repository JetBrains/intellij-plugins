terraform {
  required_providers {
    datadog = {
      source = "DataDog/datadog"
    }
  }
}

provider "datadog" {
  api_key = "3e673868-2d0f-4441-9b27-9517218a5a76"
  app_key = "01J2BDM4ZMYPNSC9YXBMM18HK9"
}

resource "datadog_synthetics_test" "example" {
  locations  = ["aws:us-east-1"]
  name      = "Internal"
  status    = "live"
  type      = "api"
}

