# intention: "HCLLiteralValidness"
# fix: "Replace with double-quotes"
# position: 15: "'var'"
#
terraform {
  required_providers {
    github = {
      source = 'hashicorp/github'
    }

    helm = {
      source = 'hashicorp/helm'
    }
    rke = {
      source  = 'rancher/rke'
    }
  }
}

variable "var" {
  type = bool
}
