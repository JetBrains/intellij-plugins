resource "google_compute_instance" "block" {
  network_interface {
    access_config {}
  }
}

resource "google_compute_instance" "object" {
  network_interface {
    access_config = {}
  }
}

resource "null_resource" "test" {
  connection {
    host1 = "${google_compute_instance.block.network_interface.0.access_config.0.nat_ip}"
    host2 = "${google_compute_instance.object.network_interface.0.access_config.0.nat_ip}"
  }
}


