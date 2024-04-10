module "main" {
  source = "./modules/fake_module"

  fake_var_1 = ""
  fake_var_2 = ""

  fake_var_4 = ""
}

module "this" {
  source    = "cloudposse/label/null"
  version   = "0.25.0"
  namespace = "em"
  stage     = "test-stage"
  name      = "test-name"
  delimiter = "-"
}
