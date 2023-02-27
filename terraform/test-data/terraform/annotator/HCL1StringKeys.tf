resource aws_cloudfront_distribution xxx {
  "enabled" = false
  default_cache_behavior {
    "viewer_protocol_policy" = ""
    "forwarded_values" {
      query_string = false
      cookies {
        forward = ""
      }
    }
  }
}

terraform {
  required_version="0.11"
}
