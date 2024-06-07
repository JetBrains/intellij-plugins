terraform {
  required_providers {
    aws = {
      source  = "figma/aws-4-49-0"
      version = "> 0.0.1"
    }
  }
}

variable "task_service_registers" {
  default = {
    "a" = "val1",
  }
}
resource "aws_ecs_service" "task_service" {
  dynamic "service_registries" {
    for_each = var.task_service_registers
    iterator = registry
    content {
      registry_arn = aws_service_discovery_service.service_discovery[registry.key].arn
      container_name = registry.value
      container_name2 = " ${registry.value} "
    }
  }
}

resource "aws_service_discovery_service" "service_discovery" {
}