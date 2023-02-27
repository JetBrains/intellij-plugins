data "terraform_remote_state" "remote" {
  backend = ""
}
module "staging" {
  source = ""
  docker_repo_url="${data.terraform_remote_state.remote.config.docker_repo_url}"
}

resource "aws_launch_configuration" "launch_config" {
  key_name = "${data.terraform_remote_state.remote.config.ssh_key_name.a.b.c}"
}