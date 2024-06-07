# intention: "TfUnknownProperty"
# fix: "Remove unknown property"
# position: 14: "ami_mot = """
#
resource "azurerm_orchestrated_virtual_machine_scale_set" "aws1" {
  location = ""
  name = ""
  platform_fault_domain_count = 0
  resource_group_name = ""
  non_property = ""

  data_disk {
    disk_size_gb         = 0
    lun                  = 0
    storage_account_type = ""
    caching              = ""
  }

}
