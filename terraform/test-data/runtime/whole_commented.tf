#provider "aws" {
#  region = "us-west-1"
#}
#
#resource <caret>"aws_instance" "web" {
#  ami           = "ami-0c55b159cbfafe1f0"
#  instance_type = "t2.micro"
#}
#
#resource "aws_security_group" "web_sg" {
#  name        = "web_sg"
#  description = "Allow incoming HTTP and SSH traffic"
#
#  ingress {
#    from_port = 80
#    to_port   = 80
#    protocol  = "tcp"
#  }
#
#  ingress {
#    from_port = 22
#    to_port   = 22
#    protocol  = "tcp"
#  }
#
#  egress {
#    from_port = 0
#    to_port   = 0
#    protocol  = "-1"
#  }
#}