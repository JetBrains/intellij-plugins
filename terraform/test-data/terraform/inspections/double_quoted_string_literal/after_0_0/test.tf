# intention: "HCLLiteralValidness"
# fix: "Replace to double-quotes"
# position: 3: "'hashicorp/github'"
#
terraform {
  required_providers {
    github = {
      source = "hashicorp/github"
    }

    helm = {
      source = 'hashicorp/helm'
    }
    rke = {
      source  = 'rancher/rke'
    }
  }
}

variable 'var' {
  type = bool
}