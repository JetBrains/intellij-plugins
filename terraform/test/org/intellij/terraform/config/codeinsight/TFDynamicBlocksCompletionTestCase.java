/*
 * Copyright 2000-2019 JetBrains s.r.o.
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
