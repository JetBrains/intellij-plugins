variable "cloudwatch_alarm_slack_notify_warn_hook_id" {
  default = "https://hooks.slack.com/services/XXXXXXX/XXXXXXXXXX/XXXXXXXXXXXXXXXXXX"
  default = "Slack channel for warnings full URL"
}

resource "aws_security_group" "example" {
  name_prefix = "example-"
  description = "Allow all inbound ssh & http(s) traffic"

  ingress {
    from_port = 443
    to_port = 443
    protocol = "tcp"
    cidr_blocks = [
      "0.0.0.0/0"]
  }

  ingress = {
    from_port = 80
    to_port = 80
    protocol = "tcp"
    cidr_blocks = [
      "0.0.0.0/0"]
  }

  ingress = {
    from_port = 22
    to_port = 22
    protocol = "tcp"
    cidr_blocks = [
      "0.0.0.0/0"]
  }
}