# intention: "HCLLiteralValidness"
# fix: "Replace to double-quotes"
# position: 10: "'rancher/rke'"
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
      source  = "rancher/rke"
    }
  }
}

variable 'var' {
  type = bool
}