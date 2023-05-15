#stop
resource "aws_instance" "a" {
}
resource "aws_instance" "b" {
  count = 2
}
locals {
  #start
  id = aws_instance.a.id
  pip = aws_instance.a.private_ip
  mon = aws_instance.a.monitoring
  l_id = aws_instance.b[0].id
  l_pip = aws_instance.b[0].private_ip
  l_mon = aws_instance.b[0].monitoring
  #stop
}

resource "random_string" "random_password_count" {
  length           = 16
  special          = true
  override_special = "/@\" "
  count            = length(local.users_list)
}

resource "random_string" "random_password_single" {
  length           = 16
  special          = true
  override_special = "/@\" "
}

resource "random_string" "random_password_for_each" {
  for_each         = toset([for e in local.users_list: e.username])
  length           = 16
  special          = true
  override_special = "/@\" "
}

locals {
  #start
  # list(string)
  c1 = random_string.random_password_count.*.result
  c2 = random_string.random_password_count[*].result
  # string
  c3 = random_string.random_password_count.0.result
  c4 = random_string.random_password_count[0].result
  # list(resource(random_string)
  f1 = values(random_string.random_password_for_each)
  # list(resource(random_string)
  f2 = values(random_string.random_password_for_each)[*]
  # list(string)
  f3 = values(random_string.random_password_for_each)[*].result
  # string
  f4 = random_string.random_password_for_each["user"].result

  # string
  s1 = random_string.random_password_single.result
  # list(string)
  s2 = random_string.random_password_single.*.result
  # string
  s3 = (random_string.random_password_single.*)[0].result
  # string
  s4 = (random_string.random_password_single.*).0.result
  # stop
}
