resource "aws_alb" "main" {
  name                             = "main"
  internal                         = false
  load_balancer_type               = "application"
  security_groups                  = []
  subnets                          = []
  enable_deletion_protection       = false
  enable_http2                     = true
  enable_cross_zone_load_balancing = true
  idle_timeout                     = 60
  tags = {
    Name = "main"
  }
}