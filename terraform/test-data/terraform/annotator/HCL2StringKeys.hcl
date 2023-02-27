resource aws_cloudfront_distribution xxx {
  <error descr="Argument names must not be quoted">"enabled"</error> = false
  default_cache_behavior {
    <error descr="Argument names must not be quoted">"viewer_protocol_policy"</error> = ""
    <error descr="Argument names must not be quoted">"forwarded_values"</error> {
      query_string = false
      cookies {
        forward = ""
      }
    }
  }
}
resource "aws_glue_catalog_table" "aaa" {
  storage_descriptor {
    ser_de_info {
      name = "ADeSF"
      parameters = {
        // must be no error
        "serialization.format" = "1"
      }
    }
  }
}
resource "aws_subnet" "public" {
  tags = merge(
    {
      // must be no error
      "Public" = "true"
      "kubernetes.io/role/elb" = "1"
      "kubernetes.io/role/alb-ingress" = "1"
    }
  )
}
locals {
  k8s_owned_map = {
    format("kubernetes.io/cluster/%s", local.name) = "owned"
  }
  m = {
    (aws_ec2_tag.root_volume_tags["Name"].value) = "test"
  }
}