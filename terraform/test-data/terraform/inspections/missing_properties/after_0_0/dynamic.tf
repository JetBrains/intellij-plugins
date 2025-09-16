# intention: "HclBlockMissingProperty"
# fix: "Add missing properties"
# position: 7: "content"
#
resource "azurerm_orchestrated_virtual_machine_scale_set" "vm_set" {
  location                    = ""
  name                        = ""
  platform_fault_domain_count = 0
  resource_group_name         = ""
  dynamic "data_disk" {
    for_each = ""
    content {
      disk_size_gb = 1
      lun          = 0
      storage_account_type = "dd"
      caching = ""
    }
  }
  data_disk {
    disk_size_gb         = 0
    lun                  = 0
    storage_account_type = ""
  }
}

resource "azurerm_virtual_machine_scale_set_packet_capture" "default" {
  network_watcher_id           = ""
  virtual_machine_scale_set_id = ""
  name = ""
  //noinspection MissingProperty
  storage_location {}
  //noinspection MissingProperty
  timeouts {}
  //source {type=""} // dynamic below
  dynamic "filter" {
    for_each = [local.source_code]
    content {
      protocol = source.value.type
    }
  }
}
