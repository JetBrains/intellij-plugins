# Required
variable "node_public_ip" {
  type        = string
  description = "Public IP of compute node for Rancher cluster"
}

variable <caret>"node_internal_ip" {
  type        = string
  description = "Internal IP of compute node for Rancher cluster"
  default     = ""
}

variable "node_username" {
  type        = string
  description = "Username used for SSH access to the Rancher server cluster node"
}