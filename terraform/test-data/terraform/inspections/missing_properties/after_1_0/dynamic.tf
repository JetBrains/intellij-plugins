resource "aws_ecs_service" "ecss" {
  name = ""
  task_definition = ""
  dynamic "service_registries" {
    for_each = ""
    content {
      registry_arn = ""
    }
  }
  service_registries {
    registry_arn = ""
  }
}

resource "aws_codebuild_project" "default" {
  name = ""
  service_role = ""
  //noinspection MissingProperty
  artifacts {}
  //noinspection MissingProperty
  environment {}
  //source {type=""} // dynamic below
  dynamic "source" {
    for_each = [local.source_code]
    content {
      type = source.value.type
    }
  }
}