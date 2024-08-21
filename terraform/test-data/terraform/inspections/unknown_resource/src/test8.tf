terraform {
  required_providers {
    datadog = {
      source  = "Datadog/Datadog"
      version = "3.42.0"
    }
    dockerk = {
      source  = "calXus/doCKer"
      version = "3.0.0"
    }
    zabbix = {
      source  = "ElastiC-iNfRa/Zabbix"
      version = "1.1.3"
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

resource "dockerk_image" "img" {
  name = "img"
}

resource "zabbix_host" "host" {
  groups = []
  host = "127.0.0.1"
}