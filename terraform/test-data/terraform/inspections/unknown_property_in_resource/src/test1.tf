terraform {
  required_providers {
    datadog = {
      source = "DataDog/datadog"
    }
  }
}

provider "datadog" {
  api_key = "0a000000-0a0a-0000-0a00-0000000a0a00"
  app_key = "00A0AAA0AAAAAAA0AAAAA00AA0"
}

resource "datadog_synthetics_test" "example" {
  locations  = ["aws:us-east-1"]
  name      = "Internal"
  status    = "live"
  type      = "api"
}

