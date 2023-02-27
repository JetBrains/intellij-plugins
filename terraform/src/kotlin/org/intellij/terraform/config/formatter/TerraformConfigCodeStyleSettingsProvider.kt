/*
 * Copyright 2000-2017 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.intellij.terraform.config.formatter

import org.intellij.terraform.hcl.formatter.HCLLanguageCodeStyleSettingsProvider
import org.intellij.terraform.config.TerraformLanguage

class TerraformConfigCodeStyleSettingsProvider : HCLLanguageCodeStyleSettingsProvider(TerraformLanguage) {
  companion object {
    private const val SAMPLE: String = """
# Specify the provider and access details
provider "aws" {
    region = "${"$"}{var.aws_region}"
}

resource "aws_elb" "web" {
  name = "terraform-example-elb"

  # The same availability zone as our instances
  availability_zones = ["${"$"}{aws_instance.web.*.availability_zone}"]

  listener {
    instance_port = 80
    instance_protocol = "http"
    lb_port = 80
    lb_protocol = "http"
  }

  # The instances are registered automatically
  instances = ["${"$"}{aws_instance.web.*.id}"]
}


resource "aws_instance" "web" {
  instance_type = "m1.small"
  ami = "${"$"}{lookup(var.aws_amis, var.aws_region)}"

  # This will create 4 instances
  count = 4
}
  """
  }

  override fun getCodeSample(settingsType: SettingsType): String {
    return SAMPLE + "\n" + super.getCodeSample(settingsType)
  }
}
