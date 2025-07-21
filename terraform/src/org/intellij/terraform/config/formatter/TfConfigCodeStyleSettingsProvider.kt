// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.formatter

import com.intellij.lang.Language
import org.intellij.terraform.config.TerraformLanguage
import org.intellij.terraform.hcl.formatter.HclLanguageCodeStyleSettingsProvider

internal class TfConfigCodeStyleSettingsProvider : HclLanguageCodeStyleSettingsProvider() {
  override fun getCodeSample(settingsType: SettingsType): String = SAMPLE.trimIndent()
  override fun getLanguage(): Language = TerraformLanguage
}

private const val SAMPLE: String = $$"""
  # Specify the provider and access details
  provider "aws" {
      region = "${var.aws_region}"
  }
  
  resource "aws_elb" "web" {
    name = "terraform-example-elb"
  
    # The same availability zone as our instances
    availability_zones = ["${aws_instance.web.*.availability_zone}"]
  
    listener {
      instance_port = 80
      instance_protocol = "http"
      lb_port = 80
      lb_protocol = "http"
    }
  
    # The instances are registered automatically
    instances = ["${aws_instance.web.*.id}"]
  }
  
  resource "aws_instance" "web" {
    instance_type = "m1.small"
    ami = "${lookup(var.aws_amis, var.aws_region)}"
  
    # This will create 4 instances
    count = 4
  }
"""
