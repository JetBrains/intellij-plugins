// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.codeinsight;

public class TFDynamicBlocksCompletionTestCase extends TFBaseCompletionTestCase {
  public void testResourceForEachIteratorCompletion() {
    doBasicCompletionTest("""
                            resource "aws_security_group" "x" {
                            dynamic "ingress" {
                            for_each = local.admin_locations
                            content {cidr_blocks = <caret>}
                            }}""", "ingress");

    doBasicCompletionTest("""
                            resource "aws_security_group" "x" {
                            dynamic "ingress" {
                            for_each = local.admin_locations
                            iterator = location
                            content {cidr_blocks = <caret>}
                            }}""", "location");

    doBasicCompletionTest("""
                            resource "aws_security_group" "x" {
                            dynamic "ingress" {
                            for_each = local.admin_locations
                            content {cidr_blocks = <caret>aaa.value}
                            }}""", "ingress");

    doBasicCompletionTest("""
                            resource "aws_security_group" "x" {
                            dynamic "ingress" {
                            for_each = local.admin_locations
                            iterator = location
                            content {cidr_blocks = <caret>aaa.value}
                            }}""", "location");
  }

  public void testResourceSelectFromForEachIteratorCompletion() {
    doBasicCompletionTest("""
                            resource "aws_security_group" "x" {
                            dynamic "ingress" {
                            for_each = local.admin_locations
                            content {cidr_blocks = ingress.<caret>}
                            }}""", "key", "value");

    doBasicCompletionTest("""
                            resource "aws_security_group" "x" {
                            dynamic "ingress" {
                            for_each = local.admin_locations
                            iterator = location
                            content {cidr_blocks = location.<caret>}
                            }}""", "key", "value");
  }

  public void testResourceSelectFromForEachValueIteratorCompletion() {
    String locals = """
      locals {
        admin_locations = [
          {
            cidr_range = "xx.xxx.xxx.xxx/xx",
            description = "Office"
          }
        ]
      }""";
    doBasicCompletionTest(locals + "resource \"aws_security_group\" \"x\" {\n" +
        "dynamic \"ingress\" {\n" +
        "for_each = local.admin_locations\n" +
        "content {cidr_blocks = ingress.value.<caret>}\n" +
        "}}", "cidr_range", "description");
    doBasicCompletionTest(locals + "resource \"aws_security_group\" \"x\" {\n" +
        "dynamic \"ingress\" {\n" +
        "for_each = local.admin_locations\n" +
        "iterator = location\n" +
        "content {cidr_blocks = location.value.<caret>}\n" +
        "}}", "cidr_range", "description");
  }
}
