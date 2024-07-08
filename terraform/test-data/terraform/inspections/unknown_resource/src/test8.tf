terraform {
  required_providers {
    datadog = {
      source = "DataDog/datadog"
    }
  }
}

provider "datadog" {
  api_key = var.datadog_api_key
  app_key = var.datadog_app_key
}

resource "datadog_synthetics_test" "example" {
  locations  = ["aws:us-east-1"]
  name      = "Internal"
  status    = "live"
  type      = "api"
}
