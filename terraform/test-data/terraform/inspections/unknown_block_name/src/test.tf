resource "google_compute_instance" "mesos-slave" {
  network_interface {
    network = "default"
    access_config {
        // Ephemeral IP
    }
    abracadabra {
    }
  }
}

terraform {
  required_version = "> 0.8.0"
  experiments = [variable_validation]
  required_providers {
    local = "~> 1.2"
    null = "~> 2.1"
  }
}

moved {
  from = test1 
  to   = test2
}

terraform {
  cloud {
    organization = ""
    workspaces {
      name = "ddd"
    }
  }
}
