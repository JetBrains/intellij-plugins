# intention: "TfUnknownResource"
# fix: "Add provider to required providers"
# position: 0: "resource "aci_cloud_ad" "cloud_ad" {"
#
terraform {
  required_providers {
    aci = {
      source  = "ciscodevnet/aci"
      version = "2.14.0"
    }
  }
}
resource "aci_cloud_ad" "cloud_ad" {
  active_directory_id = "ad_id"
  tenant_dn           = "id1"
}