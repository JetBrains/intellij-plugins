terraform {
  required_providers {
    aci = {
      source  = "CiscoDevNet/aci"
      version = "2.17.0"
    }
  }
}
resource "aci_cloud_ad" "cloud_ad" {
  active_directory_id = "ad_id"
  tenant_dn           = "id1"
}
