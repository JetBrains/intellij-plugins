variable "developers" {
  default = {
    user1 = "toto"
    user2 = "titi"
    user3 = "tata"
  }
}

resource "template_file" "test" {
  template = "${devs}"
  vars {
    devs = "${join("\\",\\"", values(var.developers))}"
  }
}

output "out" {
  value = "${template_file.test.rendered}"
}