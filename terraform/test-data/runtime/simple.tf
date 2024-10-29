provider <caret>"aws" {
  region = "us-east-1"
}

resource "aws_instance" "example" {
  ami = "ami-0c55b159cbfafe1f0"
  instance_type = "t2.micro"
}

output "instance_ip" {
  value = aws_instance.example.public_ip
}