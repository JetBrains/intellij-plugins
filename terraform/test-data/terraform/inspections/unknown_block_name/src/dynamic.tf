//noinspection MissingProperty
resource "aws_codebuild_project" "default" {
  dynamic "source" {
    for_each = [local.source_code]
    //noinspection MissingProperty
    content {
      dynamic "auth" {
        for_each = lookup(source.value, "auth", [])
        content {
          resource = lookup(auth.value, "resource", null)
          type = auth.value.type
        }
      }
    }
  }
}

//noinspection MissingProperty
resource "aws_ec2_fleet" "fleet" {
  //noinspection MissingProperty
  launch_template_config {
    dynamic "override" {
      for_each = local.overrides
      content {
        instance_type = override.value
      }
    }
  }
}