resource "aws_instance" "a" {
  ami = ""
  instance_type = ""
  provisioner "file" {
    destination = "${aws_instance.a.abracadabra}"
    dst = "${aws_instance.aa.*.abracadabra}"
    source = "${aws_instance.a.id}"
  }
}
