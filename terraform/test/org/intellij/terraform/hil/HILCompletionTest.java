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
package org.intellij.terraform.hil;

import com.intellij.lang.Language;
import org.intellij.terraform.TerraformTestUtils;
import org.intellij.terraform.config.CompletionTestCase;
import org.intellij.terraform.config.model.Function;
import org.intellij.terraform.config.model.TypeModelProvider;
import org.intellij.terraform.hil.codeinsight.HILCompletionContributor;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.assertj.core.api.BDDAssertions.then;

public class HILCompletionTest extends CompletionTestCase {
  @Override
  protected String getTestDataPath() {
    return TerraformTestUtils.getTestDataRelativePath();
  }

  @Override
  protected String getFileName() {
    return "a.tf";
  }

  @Override
  protected Language getExpectedLanguage() {
    return HILLanguage.INSTANCE;
  }

  public void testMethodCompletion_BeginOnInterpolation() throws Exception {
    doBasicCompletionTest("a='${<caret>}'", getPartialMatcher(getGlobalAvailable()));
  }

  public void testMethodCompletion_AsParameter() throws Exception {
    doBasicCompletionTest("a='${foo(<caret>)}'", getPartialMatcher(getGlobalAvailable()));
    doBasicCompletionTest("a='${foo(true,<caret>)}'", getPartialMatcher(getGlobalAvailable()));
  }

  @NotNull
  private SortedSet<String> getGlobalAvailable() {
    final TreeSet<String> result = new TreeSet<>();
    final Collection<Function> functions = TypeModelProvider.getModel(getProject()).getFunctions();
    for (Function function : functions) {
      result.add(function.getName());
    }
    result.addAll(HILCompletionContributor.GLOBAL_SCOPES);
    return result;
  }

  public void testNoMethodCompletion_InSelect() throws Exception {
    doBasicCompletionTest("a='${foo.<caret>}'", 0);
  }

  public void testSimpleVariableNoCompletion() throws Exception {
    doBasicCompletionTest("a='${var.<caret>}'", 0);
  }

  public void testSimpleVariableCompletion() throws Exception {
    doBasicCompletionTest("variable 'x' {}\na='${var.<caret>}'", 1, "x");
  }

  public void testSimpleVariableDefaultCompletion() throws Exception {
    doBasicCompletionTest("variable 'x' {default={a=true b=false}}\nfoo='${var.<caret>}'", 1, "x");
  }

  public void testSimpleVariableCompletionXY() throws Exception {
    doBasicCompletionTest("variable 'x' {}\nvariable 'y' {}\na='${var.<caret>}'", 2, "x", "y");
  }

  public void testSimpleVariableCompletionConcat() throws Exception {
    doBasicCompletionTest("variable 'x' {}\nvariable 'y' {}\na='${concat(var.<caret>)}'", 2, "x", "y");
  }

  public void testMappingVariableCompletion() throws Exception {
    doBasicCompletionTest("variable 'x' {default={a=true b=false}}\nfoo='${var.x.<caret>}'", 2, "a", "b");
  }

  public void testMappingVariableUsingTypeCompletion() throws Exception {
    doBasicCompletionTest("variable 'x' {type=object({a=bool, b=bool})}\nfoo='${var.x.<caret>}'", 2, "a", "b");
  }

  public void testMappingVariableUsingTypeCompletionTuple() throws Exception {
    doBasicCompletionTest("variable 'x' {type=tuple([object({a=bool, b=bool})])}\nfoo='${var.x[0].<caret>}'", 2, "a", "b");
  }

  public void testSelfReferenceCompletion() throws Exception {
    doBasicCompletionTest("resource 'aws_instance' 'x' {provisioner 'file' {file = '${self.<caret>}'}", "ami", "instance_type");
  }

  public void testSelfReferenceCompletionAbracadabraProvisioner() throws Exception {
    doBasicCompletionTest("resource 'abracadabra' 'x' {provisioner 'file' {file = '${self.<caret>}'}", "count");
  }

  public void testSelfReferenceCompletionAbracadabra() throws Exception {
    doBasicCompletionTest("resource 'abracadabra' 'x' {file = '${self.<caret>}'", 0);
  }

  public void testResourceNoForEachCompletion() throws Exception {
    doBasicCompletionTest("resource 'null_resource' 'x' { id = \"${<caret>}\"}", Matcher.not("each"));
  }

  public void testResourceForEachCompletion() throws Exception {
    doBasicCompletionTest("resource 'null_resource' 'x' { for_each={}\n id = \"${<caret>}\"}", "each");
  }

  public void testResourceForEachCompletionEachValues() throws Exception {
    doBasicCompletionTest("resource 'null_resource' 'x' { for_each={}\n id = \"${each.<caret>}\"}", 2, "key", "value");
  }

  public void testPathCompletion() throws Exception {
    doBasicCompletionTest("a='${path.<caret>}'", "cwd", "module", "root");
  }

  public void testCountCompletionAfterCount() throws Exception {
    doBasicCompletionTest("resource 'y' 'x' {count = 2 source='${count.<caret>}'", 1, "index");
  }

  public void testCountCompletion() throws Exception {
    doBasicCompletionTest("resource 'y' 'x' {source='${count.<caret>}'", 1, "index");
  }

  public void testCountCompletionPlusOne() throws Exception {
    doBasicCompletionTest("resource 'y' 'x' {count = 2 source='${count.<caret> + 1}'", 1, "index");
  }

  public void testCountCompletionDataAfterCount() throws Exception {
    doBasicCompletionTest("data 'y' 'x' {count = 2 source='${count.<caret>}'", 1, "index");
  }

  public void testCountCompletionData() throws Exception {
    doBasicCompletionTest("data 'y' 'x' {source='${count.<caret>}'", 1, "index");
  }

  public void testCountCompletionDataPlusOne() throws Exception {
    doBasicCompletionTest("data 'y' 'x' {count = 2 source='${count.<caret> + 1}'", 1, "index");
  }

  public void testModuleScopeCompletion() throws Exception {
    doBasicCompletionTest("module 'ref' {source = './child'} foo='${<caret>}'", "module");
  }

  public void testModuleNameCompletion() throws Exception {
    doBasicCompletionTest("module 'ref' {source = './child'} foo='${module.<caret>}'", 1, "ref");
  }

  public void testModuleInputNotCompleted() throws Exception {
    doBasicCompletionTest("module 'ref' {source = './child' x=true} foo='${module.ref.<caret>}'", 0);
  }

  //<editor-fold desc="Resource completion">
  public void testResourceTypeCompletion() throws Exception {
    doBasicCompletionTest("resource 'res_a' 'b' {} foo='${<caret>}'", "res_a");
    doBasicCompletionTest("resource 'res_a' 'b' {} foo='${concat(<caret>)}'", "res_a");
  }

  public void testResourceNameCompletion() throws Exception {
    doBasicCompletionTest("resource 'res_a' 'b' {} foo='${res_a.<caret>}'", "b");
  }

  public void testResourceNameCompletionConcat() throws Exception {
    doBasicCompletionTest("resource 'res_a' 'b' {} foo='${concat(res_a.<caret>)}'", "b");
  }

  public void testResourceNameCompletionData() throws Exception {
    doBasicCompletionTest("resource 'res_a' 'b' {}\ndata 'data_a' 'c' {}\n foo='${res_a.<caret>}'",
                          Matcher.and(Matcher.all("b"), Matcher.not("c")));
  }

  public void testResourcePropertyCompletion() throws Exception {
    doBasicCompletionTest("resource 'res_a' 'b' {x='y'} foo='${res_a.b.<caret>}'", "count", "x");
    doBasicCompletionTest("resource 'res_a' 'b' {x='y'} foo='${concat(res_a.b.<caret>)}'", "count", "x");
  }

  public void testResourcePropertyCompletionAfterNumber() throws Exception {
    doBasicCompletionTest("resource 'res_a' 'b' {x='y'} foo='${res_a.b.1.<caret>}'", "count", "x");
    doBasicCompletionTest("resource 'res_a' 'b' {x='y'} foo='${concat(res_a.b.1.<caret>)}'", "count", "x");
  }

  public void testResourcePropertyCompletionAfterStar() throws Exception {
    doBasicCompletionTest("resource 'res_a' 'b' {x='y'} foo='${res_a.b.*.<caret>}'", "count", "x");
    doBasicCompletionTest("resource 'res_a' 'b' {x='y'} foo='${concat(res_a.b.*.<caret>)}'", "count", "x");
  }

  public void testResourcePropertyCompletionAfterQuotedStar() throws Exception {
    doBasicCompletionTest("resource 'res_a' 'b' {x='y'} foo='${res_a.b.\"*\".<caret>}'", "count", "x");
    doBasicCompletionTest("resource 'res_a' 'b' {x='y'} foo='${concat(res_a.b.\"*\".<caret>)}'", "count", "x");
  }

  public void testResourcePropertyCompletionQuotedResourceName() throws Exception {
    doBasicCompletionTest("resource 'res_a' 'b' {x='y'} foo='${res_a.\"b\".<caret>}'", 0);
    doBasicCompletionTest("resource 'res_a' 'b' {x='y'} foo='${concat(res_a.\"b\".<caret>)}'", 0);
  }
  //</editor-fold>


  //<editor-fold desc="Data source completion">
  public void testDataSourceTypeCompletion() throws Exception {
    doBasicCompletionTest("data 'data_a' 'b' {} foo='${data.<caret>}'", "data_a");
    doBasicCompletionTest("data 'data_a' 'b' {} foo='${concat(data.<caret>)}'", "data_a");
  }

  public void testDataSourceNameCompletion() throws Exception {
    doBasicCompletionTest("data 'data_a' 'b' {} foo='${data.data_a.<caret>}'", 1, "b");
  }

  public void testDataSourceNameCompletionConcat() throws Exception {
    doBasicCompletionTest("data 'data_a' 'b' {} foo='${concat(data.data_a.<caret>)}'", 1, "b");
  }

  public void testDataSourceNameCompletionResource() throws Exception {
    doBasicCompletionTest("data 'data_a' 'b' {}\nresource 'res_a' 'a' {}\nfoo='${data.data_a.<caret>}'", 1, "b");
  }

  public void testDataSourcePropertyCompletion() throws Exception {
    doBasicCompletionTest("data 'data_a' 'b' {x='y'} foo='${data.data_a.b.<caret>}'", "count", "x");
    doBasicCompletionTest("data 'data_a' 'b' {x='y'} foo='${concat(data.data_a.b.<caret>)}'", "count", "x");
  }

  public void testDataSourcePropertyCompletionAfterNumber() throws Exception {
    doBasicCompletionTest("data 'data_a' 'b' {x='y'} foo='${data.data_a.b.1.<caret>}'", "count", "x");
    doBasicCompletionTest("data 'data_a' 'b' {x='y'} foo='${concat(data.data_a.b.1.<caret>)}'", "count", "x");
  }

  public void testDataSourcePropertyCompletionAfterSelect() throws Exception {
    doBasicCompletionTest("data 'data_a' 'b' {x='y'} foo='${data.data_a.b[1].<caret>}'", "count", "x");
    doBasicCompletionTest("data 'data_a' 'b' {x='y'} foo='${concat(data.data_a.b[1].<caret>)}'", "count", "x");
  }

  public void testDataSourcePropertyCompletionAfterStar() throws Exception {
    doBasicCompletionTest("data 'data_a' 'b' {x='y'} foo='${data.data_a.b.*.<caret>}'", "count", "x");
    doBasicCompletionTest("data 'data_a' 'b' {x='y'} foo='${concat(data.data_a.b.*.<caret>)}'", "count", "x");
  }

  public void testDataSourcePropertyCompletionAfterQuotedStar() throws Exception {
    doBasicCompletionTest("data 'data_a' 'b' {x='y'} foo='${data.data_a.b.\"*\".<caret>}'", "count", "x");
    doBasicCompletionTest("data 'data_a' 'b' {x='y'} foo='${concat(data.data_a.b.\"*\".<caret>)}'", "count", "x");
  }

  public void testDataSourcePropertyCompletionQuotedDataSourceName() throws Exception {
    doBasicCompletionTest("data 'data_a' 'b' {x='y'} foo='${data.data_a.\"b\".<caret>}'", 0);
    doBasicCompletionTest("data 'data_a' 'b' {x='y'} foo='${concat(data.data_a.\"b\".<caret>)}'", 0);
  }
  //</editor-fold>

  public void testSpecial_HasDynamicAttributes_Property_Not_Advised() throws Exception {
    doBasicCompletionTest("""
                            data "terraform_remote_state" "x" {}
                            output "a" {
                              value = "${data.terraform_remote_state.x.<caret>}"
                            }""", strings -> {
      then(strings).contains("backend").doesNotContain("__has_dynamic_attributes");
      return true;
    });
  }

  public void testLocalCompletion() throws Exception {
    doBasicCompletionTest("foo='${<caret>}'", "local");
  }

  public void testLocalCompletionAfter() throws Exception {
    doBasicCompletionTest("foo='${local.<caret>}'", 0);
  }

  public void testLocalsCompletion() throws Exception {
    doBasicCompletionTest("locals {x='y'} foo='${local.<caret>}'", "x");
  }

  public void testLocals2Completion() throws Exception {
    doBasicCompletionTest("locals {x='y' a=true} foo='${local.<caret>}'", "x", "a");
  }

  public void testDefinedComputedBlockCompletion() {
    doBasicCompletionTest("""
                            resource "google_dataproc_cluster" "x" {
                              cluster_config {
                                master_config {
                                  num_instances = 1
                                  machine_type = "n1-standard-1"
                                  disk_config {
                                    boot_disk_size_gb = 10
                                    num_local_ssds = 0
                                  }
                                }
                              \s
                              }
                            }

                            output "x" {
                              value = "${google_dataproc_cluster.x.cluster_config.master_config.<caret>}"
                            }""", "disk_config", "instance_names", "machine_type", "num_instances");
  }
}
