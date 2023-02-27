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
package org.intellij.terraform.config.codeinsight;

import com.intellij.openapi.util.registry.Registry;
import org.intellij.terraform.config.model.*;

import java.util.*;
import java.util.function.Predicate;

import static org.assertj.core.api.BDDAssertions.then;
import static org.intellij.terraform.config.CompletionTestCase.Matcher.*;

@SuppressWarnings({"ArraysAsListWithZeroOrOneArgument", "RedundantThrows"})
public class TerraformConfigCompletionTest extends TFBaseCompletionTestCase {

  public void testBlockKeywordCompletion() throws Exception {
    doBasicCompletionTest("<caret> {}", TerraformConfigCompletionContributor.ROOT_BLOCK_KEYWORDS);
    doBasicCompletionTest("a=1\n<caret> {}", TerraformConfigCompletionContributor.ROOT_BLOCK_KEYWORDS);

    doBasicCompletionTest("<caret> ", TerraformConfigCompletionContributor.ROOT_BLOCK_KEYWORDS);
    doBasicCompletionTest("a=1\n<caret> ", TerraformConfigCompletionContributor.ROOT_BLOCK_KEYWORDS);

    doBasicCompletionTest("\"<caret>\" {}", TerraformConfigCompletionContributor.ROOT_BLOCK_KEYWORDS);
    doBasicCompletionTest("\"<caret> {}", TerraformConfigCompletionContributor.ROOT_BLOCK_KEYWORDS);
    doBasicCompletionTest("a=1\n\"<caret>\" {}", TerraformConfigCompletionContributor.ROOT_BLOCK_KEYWORDS);
    doBasicCompletionTest("a=1\n\"<caret> {}", TerraformConfigCompletionContributor.ROOT_BLOCK_KEYWORDS);

    doBasicCompletionTest("\"<caret>\" ", TerraformConfigCompletionContributor.ROOT_BLOCK_KEYWORDS);
    doBasicCompletionTest("\"<caret> ", TerraformConfigCompletionContributor.ROOT_BLOCK_KEYWORDS);
    doBasicCompletionTest("a=1\n\"<caret>\" ", TerraformConfigCompletionContributor.ROOT_BLOCK_KEYWORDS);
    doBasicCompletionTest("a=1\n\"<caret> ", TerraformConfigCompletionContributor.ROOT_BLOCK_KEYWORDS);
  }

  public void testNoBlockKeywordCompletion() throws Exception {
    doBasicCompletionTest("a={\n<caret>\n}", 0);
  }

  //<editor-fold desc="Resources completion tests">
  public void testResourceTypeCompletion() throws Exception {
    final TreeSet<String> set = new TreeSet<>();
    for (ResourceType resource : TypeModelProvider.Companion.getModel(getProject()).getResources()) {
      set.add(resource.getType());
    }
    final Predicate<Collection<String>> matcher = getPartialMatcher(new ArrayList<>(set).subList(0, 500));
    doBasicCompletionTest("resource <caret>", matcher);
    doBasicCompletionTest("resource <caret> {}", matcher);
    doBasicCompletionTest("resource <caret> \"aaa\" {}", matcher);
    doBasicCompletionTest("\"resource\" <caret>", matcher);
    doBasicCompletionTest("\"resource\" <caret> {}", matcher);
    doBasicCompletionTest("\"resource\" <caret> \"aaa\" {}", matcher);
  }

  public void testResourceQuotedTypeCompletion() throws Exception {
    final TreeSet<String> set = new TreeSet<>();
    for (ResourceType resource : TypeModelProvider.Companion.getModel(getProject()).getResources()) {
      set.add(resource.getType());
    }
    final Predicate<Collection<String>> matcher = getPartialMatcher(new ArrayList<>(set).subList(0, 500));
    doBasicCompletionTest("resource \"<caret>", matcher);
    doBasicCompletionTest("resource '<caret>", matcher);
    doBasicCompletionTest("resource \"<caret>\n{}", matcher);
    doBasicCompletionTest("resource '<caret>\n{}", matcher);
    doBasicCompletionTest("resource \"<caret>\" {}", matcher);
    doBasicCompletionTest("resource \"<caret>\" \"aaa\" {}", matcher);
    doBasicCompletionTest("\"resource\" \"<caret>", matcher);
    doBasicCompletionTest("\"resource\" '<caret>", matcher);
    doBasicCompletionTest("\"resource\" \"<caret>\n{}", matcher);
    doBasicCompletionTest("\"resource\" '<caret>\n{}", matcher);
    doBasicCompletionTest("\"resource\" \"<caret>\" {}", matcher);
    doBasicCompletionTest("\"resource\" \"<caret>\" \"aaa\" {}", matcher);
  }

  public void testResourceCommonPropertyCompletion() throws Exception {
    doBasicCompletionTest("resource abc {\n<caret>\n}", COMMON_RESOURCE_PROPERTIES);
    final HashSet<String> set = new HashSet<>(COMMON_RESOURCE_PROPERTIES);
    set.remove("id");
    doBasicCompletionTest("resource \"x\" {\nid='a'\n<caret>\n}", set);
    doBasicCompletionTest("resource abc {\n<caret> = true\n}", Collections.emptySet());
    doBasicCompletionTest("resource abc {\n<caret> {}\n}", Arrays.asList("lifecycle", "connection", "provisioner", "dynamic"));
    doBasicCompletionTest("resource abc {\n<caret>count = 2\n}", 1, "count");
    doBasicCompletionTest("resource abc {\n\"<caret>count\" = 2\n}", 1, "count");
    // TODO: Fix mixed id-string and uncomment next line
    //doBasicCompletionTest("resource abc {\n<caret>\"count\" = 2\n}", 1, "count");
    doBasicCompletionTest("resource abc {\n<caret>lifecycle {}\n}", Arrays.asList("lifecycle", "connection", "provisioner", "dynamic"));
  }

  public void testResourceDynamicCompletion() throws Exception {
    doBasicCompletionTest("resource abc {\n dynamic x {<caret>}\n}", "for_each", "labels", "iterator", "content");
    doBasicCompletionTest("resource abc {\n dynamic <caret> \n}", not("lifecycle", "provisioner", "dynamic"));

//    doBasicCompletionTest("resource abc {\n dynamic <caret> {}\n}", not("lifecycle", "provisioner", "dynamic"));
  }

  public void testResourceForEachCompletion() throws Exception {
    doBasicCompletionTest("resource 'null_resource' 'x' { id = <caret>}", not("each"));
    doBasicCompletionTest("resource 'null_resource' 'x' { for_each={}\n id = <caret>}", "each");
    doBasicCompletionTest("resource 'null_resource' 'x' { for_each={}\n id = each.<caret>}", 2, "key", "value");
  }

  public void testResourceCommonPropertyCompletionFromModel() throws Exception {
    final HashSet<String> base = new HashSet<>(COMMON_RESOURCE_PROPERTIES);
    final ResourceType type = TypeModelProvider.Companion.getModel(getProject()).getResourceType("aws_instance");
    assertNotNull(type);
    for (PropertyOrBlockType it : type.getProperties().values()) {
      if (it.getConfigurable()) base.add(it.getName());
    }
    doBasicCompletionTest("resource aws_instance x {\n<caret>\n}", base);
    doBasicCompletionTest("resource aws_instance x {\n<caret> = \"name\"\n}", "provider", "ami");
    doBasicCompletionTest("resource aws_instance x {\n<caret> = true\n}", "ebs_optimized", "monitoring");
    doBasicCompletionTest("resource aws_instance x {\n<caret> {}\n}", "lifecycle");

    doBasicCompletionTest("resource aws_instance x {\n\"<caret>\"\n}", base);
    doBasicCompletionTest("resource aws_instance x {\n\"<caret>\" = \"name\"\n}", "provider", "ami");
    doBasicCompletionTest("resource aws_instance x {\n\"<caret>\" = true\n}", "ebs_optimized", "monitoring");
    doBasicCompletionTest("resource aws_instance x {\n\"<caret>\" {}\n}", "lifecycle");

    // Should understand interpolation result
    doBasicCompletionTest("resource aws_instance x {\n<caret> = \"${true}\"\n}", strings -> {
      then(strings).contains("ebs_optimized", "monitoring").doesNotContain("lifecycle", "provider", "ami");
      return true;
    });
    // Or not
    doBasicCompletionTest("resource aws_instance x {\n<caret> = \"${}\"\n}", strings -> {
      then(strings).contains("ebs_optimized", "monitoring", "provider", "ami").doesNotContain("lifecycle");
      return true;
    });
  }

  public void testResourceCommonPropertyAlreadyDefinedNotShownAgain() throws Exception {
    final ResourceType type = TypeModelProvider.Companion.getModel(getProject()).getResourceType("aws_vpc_endpoint");
    assertNotNull(type);

    // Should not add existing props to completion variants
    doBasicCompletionTest("""
                            resource aws_vpc_endpoint x {
                              <caret>  service_name = ""
                              vpc_id = ""
                            }
                            """, not("service_name", "vpc_id"));
    doBasicCompletionTest("""
                            resource aws_vpc_endpoint x {
                              service_name = ""
                              <caret>  vpc_id = ""
                            }
                            """, not("service_name", "vpc_id"));
    doBasicCompletionTest("""
                            resource aws_vpc_endpoint x {
                              service_name = ""
                              vpc_id = ""
                              <caret>}
                            """, not("service_name", "vpc_id"));

    // yet should advice if we stand on it
    doBasicCompletionTest("""
                            resource aws_vpc_endpoint x {
                              service_name = ""
                              <caret>vpc_id = ""
                            }
                            """, and(not("service_name"), all("vpc_id")));
    doBasicCompletionTest("""
                            resource aws_vpc_endpoint x {
                              <caret>service_name = ""
                              vpc_id = ""
                            }
                            """, and(not("vpc_id"), all("service_name")));


  }

  public void testResourceProviderCompletionFromModel() throws Exception {
    doBasicCompletionTest("provider Z {}\nresource a b {provider=<caret>}", "Z");
    doBasicCompletionTest("provider Z {}\nresource a b {provider='<caret>'}", "Z");
    doBasicCompletionTest("provider Z {}\nresource a b {provider=\"<caret>\"}", "Z");
    doBasicCompletionTest("provider Z {alias='Y'}\nresource a b {provider=<caret>}", "Z.Y");
    doBasicCompletionTest("provider Z {alias='Y'}\nresource a b {provider='<caret>'}", "Z.Y");
    doBasicCompletionTest("provider Z {alias='Y'}\nresource a b {provider=\"<caret>\"}", "Z.Y");
  }

  public void testResourcePropertyCompletionBeforeInnerBlock() throws Exception {
    doBasicCompletionTest("resource abc {\n<caret>\nlifecycle {}\n}", COMMON_RESOURCE_PROPERTIES);
    final HashSet<String> set = new HashSet<>(COMMON_RESOURCE_PROPERTIES);
    set.remove("id");
    doBasicCompletionTest("resource \"x\" {\nid='a'\n<caret>\nlifecycle {}\n}", set);
    doBasicCompletionTest("resource abc {\n<caret> = true\nlifecycle {}\n}", Collections.emptySet());
  }

  public void testResourceDependsOnCompletion() throws Exception {
    doBasicCompletionTest("resource x y {}\nresource a b {depends_on=['<caret>']}", 1, "x.y");
    doBasicCompletionTest("resource x y {}\nresource a b {depends_on=[\"<caret>\"]}", 1, "x.y");
    doBasicCompletionTest("data x y {}\nresource a b {depends_on=['<caret>']}", 1, "data.x.y");
    doBasicCompletionTest("data x y {}\nresource a b {depends_on=[\"<caret>\"]}", 1, "data.x.y");

    doBasicCompletionTest("resource x y {}\nresource a b {depends_on=[<caret>]}", 1, "x.y");
    doBasicCompletionTest("data x y {}\nresource a b {depends_on=[<caret>]}", 1, "data.x.y");

    doBasicCompletionTest("variable v{}\nresource x y {}\nresource a b {depends_on=[<caret>]}", 2, "x.y", "var.v");
    doBasicCompletionTest("variable v{}\ndata x y {}\nresource a b {depends_on=[<caret>]}", 2, "data.x.y", "var.v");
  }

  public void testResourceTypeCompletionGivenDefinedProvidersOrForNoPropsProviders() throws Exception {
    Registry.get("ide.completion.variant.limit").setValue(2000, getTestRootDisposable());

    final TreeSet<String> set = new TreeSet<>();
    final Map<String, Boolean> cache = new HashMap<>();
    for (ResourceType resource : TypeModelProvider.Companion.getModel(getProject()).getResources()) {
      if (isExcludeProvider(resource.getProvider(), cache)) continue;
      set.add(resource.getType());
    }
    then(set).contains("template_file", "aws_vpc");
    doBasicCompletionTest("provider aws {}\nresource <caret>", set);
    doBasicCompletionTest("provider aws {}\nresource <caret> {}", set);
    doBasicCompletionTest("provider aws {}\nresource <caret> \"aaa\" {}", set);
  }

  public void testResourceNonConfigurablePropertyIsNotAdviced() throws Exception {
    doBasicCompletionTest("resource \"random_string\" \"x\" { <caret> }", strings -> {
      then(strings).doesNotContain("result");
      return true;
    });
  }
  //</editor-fold>

  //<editor-fold desc="Data Sources completion tests">
  public void testDataSourceTypeCompletion() throws Exception {
    Registry.get("ide.completion.variant.limit").setValue(100000, getTestRootDisposable());
    final TreeSet<String> set = new TreeSet<>();
    for (DataSourceType ds : TypeModelProvider.Companion.getModel(getProject()).getDataSources()) {
      set.add(ds.getType());
    }
    doBasicCompletionTest("data <caret>", set);
    doBasicCompletionTest("data <caret> {}", set);
    doBasicCompletionTest("data <caret> \"aaa\" {}", set);
  }

  public void testDataSourceQuotedTypeCompletion() throws Exception {
    Registry.get("ide.completion.variant.limit").setValue(100000, getTestRootDisposable());
    final TreeSet<String> set = new TreeSet<>();
    for (DataSourceType ds : TypeModelProvider.Companion.getModel(getProject()).getDataSources()) {
      set.add(ds.getType());
    }
    doBasicCompletionTest("data \"<caret>", set);
    doBasicCompletionTest("data '<caret>", set);
    doBasicCompletionTest("data \"<caret>\n{}", set);
    doBasicCompletionTest("data '<caret>\n{}", set);
    doBasicCompletionTest("data \"<caret>\" {}", set);
    doBasicCompletionTest("data \"<caret>\" \"aaa\" {}", set);
  }

  public void testDataSourceCommonPropertyCompletion() throws Exception {
    doBasicCompletionTest("data abc {\n<caret>\n}", COMMON_DATA_SOURCE_PROPERTIES);
    final HashSet<String> set = new HashSet<>(COMMON_DATA_SOURCE_PROPERTIES);
    set.remove("id");
    doBasicCompletionTest("data \"x\" {\nid='a'\n<caret>\n}", set);
    doBasicCompletionTest("data abc {\n<caret> = true\n}", Collections.emptySet());
    doBasicCompletionTest("data abc {\n<caret> {}\n}", 0);
  }

  public void testDataSourceCommonPropertyCompletionFromModel() throws Exception {
    final HashSet<String> base = new HashSet<>(COMMON_DATA_SOURCE_PROPERTIES);
    final DataSourceType type = TypeModelProvider.Companion.getModel(getProject()).getDataSourceType("aws_ecs_container_definition");
    assertNotNull(type);
    for (PropertyOrBlockType it : type.getProperties().values()) {
      if (it.getConfigurable()) base.add(it.getName());
    }
    doBasicCompletionTest("data aws_ecs_container_definition x {\n<caret>\n}", base);
    doBasicCompletionTest("data aws_ecs_container_definition x {\n<caret> = \"name\"\n}",
            "container_name",
            "task_definition",
            "provider"
    );
    doBasicCompletionTest("data aws_elastic_beanstalk_solution_stack x {\n<caret> = true\n}", "most_recent");
    doBasicCompletionTest("data aws_kms_secret x {\n<caret> {}\n}", "secret");

    // Should understand interpolation result
    doBasicCompletionTest("data aws_elastic_beanstalk_solution_stack x {\n<caret> = \"${true}\"\n}", strings -> {
      then(strings).contains("most_recent").doesNotContain("name", "name_regex");
      return true;
    });
  }

  public void testDataSourceProviderCompletionFromModel() throws Exception {
    doBasicCompletionTest("provider Z {}\ndata a b {provider=<caret>}", "Z");
    doBasicCompletionTest("provider Z {}\ndata a b {provider='<caret>'}", "Z");
    doBasicCompletionTest("provider Z {}\ndata a b {provider=\"<caret>\"}", "Z");
    doBasicCompletionTest("provider Z {alias='Y'}\ndata a b {provider=<caret>}", "Z.Y");
    doBasicCompletionTest("provider Z {alias='Y'}\ndata a b {provider='<caret>'}", "Z.Y");
    doBasicCompletionTest("provider Z {alias='Y'}\ndata a b {provider=\"<caret>\"}", "Z.Y");
  }

  public void testDataSourceDependsOnCompletion() throws Exception {
    doBasicCompletionTest("resource x y {}\ndata a b {depends_on=['<caret>']}", 1, "x.y");
    doBasicCompletionTest("resource x y {}\ndata a b {depends_on=[\"<caret>\"]}", 1, "x.y");
    doBasicCompletionTest("data x y {}\ndata a b {depends_on=['<caret>']}", 1, "data.x.y");
    doBasicCompletionTest("data x y {}\ndata a b {depends_on=[\"<caret>\"]}", 1, "data.x.y");

    doBasicCompletionTest("resource x y {}\ndata a b {depends_on=[<caret>]}", 1, "x.y");
    doBasicCompletionTest("data x y {}\ndata a b {depends_on=[<caret>]}", 1, "data.x.y");

    doBasicCompletionTest("variable v{}\nresource x y {}\ndata a b {depends_on=[<caret>]}", 2, "x.y", "var.v");
    doBasicCompletionTest("variable v{}\ndata x y {}\ndata a b {depends_on=[<caret>]}", 2, "data.x.y", "var.v");
  }

  public void testDataSourceTypeCompletionGivenDefinedProviders() throws Exception {
    final TreeSet<String> set = new TreeSet<>();
    final Map<String, Boolean> cache = new HashMap<>();
    for (DataSourceType ds : TypeModelProvider.Companion.getModel(getProject()).getDataSources()) {
      if (isExcludeProvider(ds.getProvider(), cache)) continue;
      set.add(ds.getType());
    }
    then(set).contains("template_file", "aws_vpc");
    doBasicCompletionTest("provider aws {}\ndata <caret>", set);
    doBasicCompletionTest("provider aws {}\ndata <caret> {}", set);
    doBasicCompletionTest("provider aws {}\ndata <caret> \"aaa\" {}", set);
  }
  //</editor-fold>

  public void testOutputDependsOnCompletion() throws Exception {
    doBasicCompletionTest("output o {<caret>}", "depends_on");

    doBasicCompletionTest("resource x y {}\noutput o {depends_on=[<caret>]}", 1, "x.y");
    doBasicCompletionTest("resource x y {}\noutput o {depends_on=['<caret>']}", 1, "x.y");
    doBasicCompletionTest("resource x y {}\noutput o {depends_on=[\"<caret>\"]}", 1, "x.y");
    doBasicCompletionTest("data x y {}\noutput o {depends_on=[<caret>]}", 1, "data.x.y");
    doBasicCompletionTest("data x y {}\noutput o {depends_on=['<caret>']}", 1, "data.x.y");
    doBasicCompletionTest("data x y {}\noutput o {depends_on=[\"<caret>\"]}", 1, "data.x.y");

    doBasicCompletionTest("variable v{}\nresource x y {}\noutput o {depends_on=[<caret>]}", 2, "x.y", "var.v");
    doBasicCompletionTest("variable v{}\ndata x y {}\noutput o {depends_on=[<caret>]}", 2, "data.x.y", "var.v");
  }

  public void testVariableTypeCompletion() throws Exception {
    myCompleteInvocationCount = 1; // Ensure there would not be 'null', 'true' and 'false' variants
    doBasicCompletionTest("variable v { type = <caret> }", 9,
                          "any", "string", "number", "bool", "list", "set", "map", "object", "tuple");
    doBasicCompletionTest("variable v { type = object(x=<caret>) }", 10,
                          "any", "string", "number", "bool", "list", "set", "map", "object", "tuple", "optional");
    doBasicCompletionTest("variable v { type = object(x=optional(<caret>)) }", 9,
                          "any", "string", "number", "bool", "list", "set", "map", "object", "tuple");
    doBasicCompletionTest("variable v { type = object(x=list(<caret>)) }", 9,
                          "any", "string", "number", "bool", "list", "set", "map", "object", "tuple");

    doBasicCompletionTest("variable v { type = object(x=<caret>optional()) }", 10,
                          "any", "string", "number", "bool", "list", "set", "map", "object", "tuple", "optional");
  }

  public void testSpecial_HasDynamicAttributes_Property_Not_Advised() throws Exception {
    doBasicCompletionTest("data \"terraform_remote_state\" \"x\" { <caret> }", strings -> {
      then(strings).contains("backend").doesNotContain("__has_dynamic_attributes");
      return true;
    });
  }

  public void testModuleProvidersPropertyCompletion() {
    myFixture.addFileToProject("module/a.tf", "provider aws {}\nprovider aws {alias=\"second\"}");
    // via PropertyObjectKeyCompletionProvider
    doBasicCompletionTest("""
                            module x {
                              source = "./module/"
                              providers = {
                                <caret>\s
                              }
                            }""", Arrays.asList("aws", "aws.second"));
    doBasicCompletionTest("""
                            module x {
                              source = "./module/"
                              providers = {
                                "<caret>"\s
                              }
                            }""", Arrays.asList("aws", "aws.second"));
    doBasicCompletionTest("""
                            module x {
                              source = "./module/"
                              providers = {
                                a<caret>\s
                              }
                            }""", Arrays.asList("aws", "aws.second"));
    doBasicCompletionTest("""
                            module x {
                              source = "./module/"
                              providers = {
                                "a<caret>"\s
                              }
                            }""", Arrays.asList("aws", "aws.second"));
    doBasicCompletionTest("""
                            module x {
                              source = "./module/"
                              providers = {
                                <caret>aws = "aws"\s
                              }
                            }""", Arrays.asList("aws", "aws.second"));
    doBasicCompletionTest("""
                            module x {
                              source = "./module/"
                              providers = {
                                "<caret>aws" = "aws"\s
                              }
                            }""", Arrays.asList("aws", "aws.second"));
    doBasicCompletionTest("""
                            module x {
                              source = "./module/"
                              providers = {
                                <caret> = "aws"\s
                              }
                            }""", Arrays.asList("aws", "aws.second"));
    doBasicCompletionTest("""
                            module x {
                              source = "./module/"
                              providers = {
                                "<caret>" = "aws"\s
                              }
                            }""", Arrays.asList("aws", "aws.second"));
    doBasicCompletionTest("""
                            module x {
                              source = "./module/"
                              providers = {
                                aws = "aws"
                                <caret>
                              }
                            }""", Arrays.asList("aws.second"));
    doBasicCompletionTest("""
                            module x {
                              source = "./module/"
                              providers = {
                                <caret>
                                aws = "aws"\s
                              }
                            }""", Arrays.asList("aws.second"));


    doBasicCompletionTest("""
                            module x {
                              source = "./module/"
                              providers = {
                                aws = <caret>\s
                              }
                            }""", 0);
  }

  public void testModuleProvidersBlockCompletion() {
    myFixture.addFileToProject("module/a.tf", "provider aws {}\nprovider aws {alias=\"second\"}");
    doBasicCompletionTest("""
                            module x {
                              source = "./module/"
                              providers {
                                <caret>\s
                              }
                            }""", Arrays.asList("aws", "aws.second"));
    doBasicCompletionTest("""
                            module x {
                              source = "./module/"
                              providers {
                                "<caret>"\s
                              }
                            }""", Arrays.asList("aws", "aws.second"));
    doBasicCompletionTest("""
                            module x {
                              source = "./module/"
                              providers {
                                a<caret>\s
                              }
                            }""", Arrays.asList("aws", "aws.second"));
    doBasicCompletionTest("""
                            module x {
                              source = "./module/"
                              providers {
                                "a<caret>"\s
                              }
                            }""", Arrays.asList("aws", "aws.second"));
    doBasicCompletionTest("""
                            module x {
                              source = "./module/"
                              providers {
                                <caret>aws = "aws"\s
                              }
                            }""", Arrays.asList("aws", "aws.second"));
    doBasicCompletionTest("""
                            module x {
                              source = "./module/"
                              providers {
                                "<caret>aws" = "aws"\s
                              }
                            }""", Arrays.asList("aws", "aws.second"));
    doBasicCompletionTest("""
                            module x {
                              source = "./module/"
                              providers {
                                <caret> = "aws"\s
                              }
                            }""", Arrays.asList("aws", "aws.second"));
    doBasicCompletionTest("""
                            module x {
                              source = "./module/"
                              providers {
                                "<caret>" = "aws"\s
                              }
                            }""", Arrays.asList("aws", "aws.second"));
    doBasicCompletionTest("""
                            module x {
                              source = "./module/"
                              providers {
                                aws = "aws"
                                <caret>
                              }
                            }""", Arrays.asList("aws.second"));
    doBasicCompletionTest("""
                            module x {
                              source = "./module/"
                              providers {
                                <caret>
                                aws = "aws"\s
                              }
                            }""", Arrays.asList("aws.second"));


    doBasicCompletionTest("""
                            module x {
                              source = "./module/"
                              providers {
                                aws = <caret>\s
                              }
                            }""", 0);
  }

  public void testModuleProvidersValueCompletion() {
    myFixture.addFileToProject("module/a.tf", "provider aws {}\nprovider aws {alias=\"second\"}");
    // via PropertyObjectKeyCompletionProvider
    doBasicCompletionTest("""
                            provider aws {}module x {
                              source = "./module/"
                              providers = {
                                aws=<caret>\s
                              }
                            }""", Arrays.asList("aws"));
    doBasicCompletionTest("""
                            provider aws {alias="first"}module x {
                              source = "./module/"
                              providers {
                                aws=<caret>\s
                              }
                            }""", Arrays.asList("aws.first"));
    doBasicCompletionTest("""
                            provider aws {alias="first"}module x {
                              source = "./module/"
                              providers {
                                aws="<caret>"\s
                              }
                            }""", Arrays.asList("aws.first"));
  }

  public void testModuleForEachCompletion() throws Exception {
    doBasicCompletionTest("module 'x' { id = <caret>}", Matcher.not("each"));
    doBasicCompletionTest("module 'x' { for_each={}\n id = <caret>}", "each");
    doBasicCompletionTest("module 'x' { for_each={}\n id = each.<caret>}", 2, "key", "value");
  }


  public void testModuleDependsOnCompletion() throws Exception {
    doBasicCompletionTest("resource x y {}\nmodule b {depends_on=['<caret>']}", 1, "x.y");
    doBasicCompletionTest("resource x y {}\nmodule b {depends_on=[\"<caret>\"]}", 1, "x.y");
    doBasicCompletionTest("data x y {}\nmodule b {depends_on=['<caret>']}", 1, "data.x.y");
    doBasicCompletionTest("data x y {}\nmodule b {depends_on=[\"<caret>\"]}", 1, "data.x.y");

    doBasicCompletionTest("resource x y {}\nmodule b {depends_on=[<caret>]}", 1, "x.y");
    doBasicCompletionTest("data x y {}\nmodule b {depends_on=[<caret>]}", 1, "data.x.y");

    doBasicCompletionTest("variable v{}\nresource x y {}\nmodule b {depends_on=[<caret>]}", 2, "x.y", "var.v");
    doBasicCompletionTest("variable v{}\ndata x y {}\nmodule b {depends_on=[<caret>]}", 2, "data.x.y", "var.v");
  }

  private boolean isExcludeProvider(ProviderType provider, Map<String, Boolean> cache) {
    String key = provider.getType();
    Boolean cached = cache.get(key);
    if (cached == null) {
      cached = true;
      if (provider.getType().equals("aws")) {
        cached = false;
      } else if (provider.getProperties().equals(TypeModel.AbstractProvider.getProperties())) {
        cached = false;
      }
      cache.put(key, cached);
    }
    return cached;
  }
}
