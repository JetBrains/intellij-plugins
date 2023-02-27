locals {
  source_code = {
    type = "NO_SOURCE"
    auth = [{
      epyt = "OAUTH"
      resource = "not null"
    }]
  }
  source_code_list = [local.source_code]
  instance_types = ["c5.large", "m5.large"]
}
//noinspection MissingProperty
resource "aws_codebuild_project" "default" {
  dynamic "source" {
    for_each = [local.source_code]
    content {
      type = source.value.type
      dynamic "auth" {
        for_each = lookup(source.value, "auth", [])
        content {
          resource = lookup(auth.value, "resource", null)
          type     = auth.value.epyt
        }
      }
    }
  }
  dynamic "source" {
    for_each = local.source_code_list
    content {
      type = source.value.type
      dynamic "auth" {
        for_each = source.value.auth
        content {
          resource = lookup(auth.value, "resource", null)
          type     = auth.value.epyt
        }
      }
    }
  }
}

//noinspection MissingProperty
resource "aws_ec2_fleet" "fleet1" {
  //noinspection MissingProperty
  launch_template_config {
    dynamic "override" {
      for_each = local.instance_types
      content {
        instance_type = override.value
      }
    }
    dynamic "override" {
      for_each = local.instance_types
      iterator = it
      content {
        instance_type = it.key
        max_price = it.unknown
      }
    }
  }
}