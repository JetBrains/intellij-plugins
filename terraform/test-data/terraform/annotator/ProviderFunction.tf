resource "aws_ecr_repository" "hashicups" {
  name = provider::aws::arn_parse("arn:aws:iam::444455556666:role/example").region

  image_scanning_configuration {
    scan_on_push = true
  }
}

output "example_output" {
  value = <error descr="Expected keyword 'provider' at the beginning of the provider-defined function">provider1</error>::kubernetes::manifest_decode(file("manifest.yaml"))
}