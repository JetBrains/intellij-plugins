variable "atlas_artifact" {
  default = {
    master = "capgemini/apollo-ubuntu-14.04-amd64"
    slave  = "capgemini/apollo-ubuntu-14.04-amd64"
  }
}

variable "atlas_artifact_version" {
  default = {
    master = "20"
    slave  = "20"
  }
}

resource "atlas_artifact" "mesos-slave" {
  name    = "${var.atlas_artifact.slave}"
  type    = "aws.ami"
  version = "${var.atlas_artifact_version.master}"
}

variable "network" {
  type = object({
    subnets = list(string)
  })
}
module sub {
  source = "./sub"
  subnets = var.network.subnets
}
output "from-module" {
  value = module.sub.sn
}