terraform {

}

provider "vkcs" {
  username   = "username"
  password   = "password"
  project_id = "project_id"
}
#==============================================================================================

data "vkcs_compute_keypair" "ssh-key" {
  name = "ssh-key-name"
}

resource "vkcs_" "vvv" {}


resource "vkcs_compute_instance" "test-vm" {
  name   = "test1"
}
