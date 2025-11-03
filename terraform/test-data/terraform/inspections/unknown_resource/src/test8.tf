terraform {}

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

resource "dockerk_image" "img" {
  name = "img"
}

resource "zabbix_host" "host" {
  groups = []
  host = "127.0.0.1"
}