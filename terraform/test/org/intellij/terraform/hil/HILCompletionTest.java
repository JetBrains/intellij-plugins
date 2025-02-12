// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.hil;

import com.intellij.lang.Language;
import org.intellij.terraform.TfTestUtils;
import org.intellij.terraform.config.CompletionTestCase;
import org.intellij.terraform.config.codeinsight.TfCompletionUtil;
import org.intellij.terraform.config.model.TfFunction;
import org.intellij.terraform.config.model.TypeModelProvider;
import org.jetbrains.annotations.NotNull;

import java.util.Collection;
import java.util.SortedSet;
import java.util.TreeSet;

import static org.assertj.core.api.BDDAssertions.then;

public class HILCompletionTest extends CompletionTestCase {
  @Override
  protected String getTestDataPath() {
    return TfTestUtils.getTestDataRelativePath();
  }

  @Override
  protected String getFileName() {
    return "a.tf";
  }

  @Override
  protected Language getExpectedLanguage() {
    return HILLanguage.INSTANCE;
  }

  public void testMethodCompletion_BeginOnInterpolation() {
    doBasicCompletionTest("a='${<caret>}'", getPartialMatcher(getGlobalAvailable()));
  }

  public void testMethodCompletion_AsParameter() {
    doBasicCompletionTest("a='${foo(<caret>)}'", getPartialMatcher(getGlobalAvailable()));
    doBasicCompletionTest("a='${foo(true,<caret>)}'", getPartialMatcher(getGlobalAvailable()));
  }

  @NotNull
  private static SortedSet<String> getGlobalAvailable() {
    final TreeSet<String> result = new TreeSet<>();
    final Collection<TfFunction> functions = TypeModelProvider.getGlobalModel().getFunctions();
    for (TfFunction function : functions) {
      result.add(function.getName());
    }
    result.addAll(TfCompletionUtil.INSTANCE.getGlobalScopes());
    return result;
  }

  public void testNoMethodCompletion_InSelect() {
    doBasicCompletionTest("a='${foo.<caret>}'", 0);
  }

  public void testSimpleVariableNoCompletion() {
    doBasicCompletionTest("a='${var.<caret>}'", 0);
  }

  public void testSimpleVariableCompletion() {
    doBasicCompletionTest("variable 'x' {}\na='${var.<caret>}'", 1, "x");
  }

  public void testSimpleVariableDefaultCompletion() {
    doBasicCompletionTest("variable 'x' {default={a=true b=false}}\nfoo='${var.<caret>}'", 1, "x");
  }

  public void testSimpleVariableCompletionXY() {
    doBasicCompletionTest("variable 'x' {}\nvariable 'y' {}\na='${var.<caret>}'", 2, "x", "y");
  }

  public void testSimpleVariableCompletionConcat() {
    doBasicCompletionTest("variable 'x' {}\nvariable 'y' {}\na='${concat(var.<caret>)}'", 2, "x", "y");
  }

  public void testMappingVariableCompletion() {
    doBasicCompletionTest("variable 'x' {default={a=true b=false}}\nfoo='${var.x.<caret>}'", 2, "a", "b");
  }

  public void testMappingVariableUsingTypeCompletion() {
    doBasicCompletionTest("variable 'x' {type=object({a=bool, b=bool})}\nfoo='${var.x.<caret>}'", 2, "a", "b");
  }

  public void testMappingVariableUsingTypeCompletionTuple() {
    doBasicCompletionTest("variable 'x' {type=tuple([object({a=bool, b=bool})])}\nfoo='${var.x[0].<caret>}'", 2, "a", "b");
  }

  public void testSelfReferenceCompletion() {
    doBasicCompletionTest("resource 'azurerm_resource_group' 'x' {provisioner 'file' {file = '${self.<caret>}'}", "name", "location");
  }

  public void testSelfReferenceCompletionAbracadabraProvisioner() {
    doBasicCompletionTest("resource 'abracadabra' 'x' {provisioner 'file' {file = '${self.<caret>}'}", "count");
  }

  public void testSelfReferenceCompletionAbracadabra() {
    doBasicCompletionTest("resource 'abracadabra' 'x' {file = '${self.<caret>}'", 0);
  }

  public void testResourceNoForEachCompletion() {
    doBasicCompletionTest("resource 'null_resource' 'x' { id = \"${<caret>}\"}", Matcher.not("each"));
  }

  public void testResourceForEachCompletion() {
    doBasicCompletionTest("resource 'null_resource' 'x' { for_each={}\n id = \"${<caret>}\"}", "each");
  }

  public void testResourceForEachCompletionEachValues() {
    doBasicCompletionTest("resource 'null_resource' 'x' { for_each={}\n id = \"${each.<caret>}\"}", 2, "key", "value");
  }

  public void testPathCompletion() {
    doBasicCompletionTest("a='${path.<caret>}'", "cwd", "module", "root");
  }

  public void testCountCompletionAfterCount() {
    doBasicCompletionTest("resource 'y' 'x' {count = 2 source='${count.<caret>}'", 1, "index");
  }

  public void testCountCompletion() {
    doBasicCompletionTest("resource 'y' 'x' {source='${count.<caret>}'", 1, "index");
  }

  public void testCountCompletionPlusOne() {
    doBasicCompletionTest("resource 'y' 'x' {count = 2 source='${count.<caret> + 1}'", 1, "index");
  }

  public void testCountCompletionDataAfterCount() {
    doBasicCompletionTest("data 'y' 'x' {count = 2 source='${count.<caret>}'", 1, "index");
  }

  public void testCountCompletionData() {
    doBasicCompletionTest("data 'y' 'x' {source='${count.<caret>}'", 1, "index");
  }

  public void testCountCompletionDataPlusOne() {
    doBasicCompletionTest("data 'y' 'x' {count = 2 source='${count.<caret> + 1}'", 1, "index");
  }

  public void testModuleScopeCompletion() {
    doBasicCompletionTest("module 'ref' {source = './child'} foo='${<caret>}'", "module");
  }

  public void testModuleNameCompletion() {
    doBasicCompletionTest("module 'ref' {source = './child'} foo='${module.<caret>}'", 1, "ref");
  }

  public void testModuleInputNotCompleted() {
    doBasicCompletionTest("module 'ref' {source = './child' x=true} foo='${module.ref.<caret>}'", 0);
  }

  //<editor-fold desc="Resource completion">
  public void testResourceTypeCompletion() {
    doBasicCompletionTest("resource 'res_a' 'b' {} foo='${<caret>}'", "res_a");
    doBasicCompletionTest("resource 'res_a' 'b' {} foo='${concat(<caret>)}'", "res_a");
  }

  public void testResourceNameCompletion() {
    doBasicCompletionTest("resource 'res_a' 'b' {} foo='${res_a.<caret>}'", "b");
  }

  public void testResourceNameCompletionConcat() {
    doBasicCompletionTest("resource 'res_a' 'b' {} foo='${concat(res_a.<caret>)}'", "b");
  }

  public void testResourceNameCompletionData() {
    doBasicCompletionTest("resource 'res_a' 'b' {}\ndata 'data_a' 'c' {}\n foo='${res_a.<caret>}'",
                          Matcher.and(Matcher.all("b"), Matcher.not("c")));
  }

  public void testResourcePropertyCompletion() {
    doBasicCompletionTest("resource 'res_a' 'b' {x='y'} foo='${res_a.b.<caret>}'", "count", "x");
    doBasicCompletionTest("resource 'res_a' 'b' {x='y'} foo='${concat(res_a.b.<caret>)}'", "count", "x");
  }

  public void testResourcePropertyCompletionAfterNumber() {
    doBasicCompletionTest("resource 'res_a' 'b' {x='y'} foo='${res_a.b.1.<caret>}'", "count", "x");
    doBasicCompletionTest("resource 'res_a' 'b' {x='y'} foo='${concat(res_a.b.1.<caret>)}'", "count", "x");
  }

  public void testResourcePropertyCompletionAfterStar() {
    doBasicCompletionTest("resource 'res_a' 'b' {x='y'} foo='${res_a.b.*.<caret>}'", "count", "x");
    doBasicCompletionTest("resource 'res_a' 'b' {x='y'} foo='${concat(res_a.b.*.<caret>)}'", "count", "x");
  }

  public void testResourcePropertyCompletionAfterQuotedStar() {
    doBasicCompletionTest("resource 'res_a' 'b' {x='y'} foo='${res_a.b.\"*\".<caret>}'", "count", "x");
    doBasicCompletionTest("resource 'res_a' 'b' {x='y'} foo='${concat(res_a.b.\"*\".<caret>)}'", "count", "x");
  }

  public void testResourcePropertyCompletionQuotedResourceName() {
    doBasicCompletionTest("resource 'res_a' 'b' {x='y'} foo='${res_a.\"b\".<caret>}'", 0);
    doBasicCompletionTest("resource 'res_a' 'b' {x='y'} foo='${concat(res_a.\"b\".<caret>)}'", 0);
  }
  //</editor-fold>


  //<editor-fold desc="Data source completion">
  public void testDataSourceTypeCompletion() {
    doBasicCompletionTest("data 'data_a' 'b' {} foo='${data.<caret>}'", "data_a");
    doBasicCompletionTest("data 'data_a' 'b' {} foo='${concat(data.<caret>)}'", "data_a");
  }

  public void testDataSourceNameCompletion() {
    doBasicCompletionTest("data 'data_a' 'b' {} foo='${data.data_a.<caret>}'", 1, "b");
  }

  public void testDataSourceNameCompletionConcat() {
    doBasicCompletionTest("data 'data_a' 'b' {} foo='${concat(data.data_a.<caret>)}'", 1, "b");
  }

  public void testDataSourceNameCompletionResource() {
    doBasicCompletionTest("data 'data_a' 'b' {}\nresource 'res_a' 'a' {}\nfoo='${data.data_a.<caret>}'", 1, "b");
  }

  public void testDataSourcePropertyCompletion() {
    doBasicCompletionTest("data 'data_a' 'b' {x='y'} foo='${data.data_a.b.<caret>}'", "count", "x");
    doBasicCompletionTest("data 'data_a' 'b' {x='y'} foo='${concat(data.data_a.b.<caret>)}'", "count", "x");
  }

  public void testDataSourcePropertyCompletionAfterNumber() {
    doBasicCompletionTest("data 'data_a' 'b' {x='y'} foo='${data.data_a.b.1.<caret>}'", "count", "x");
    doBasicCompletionTest("data 'data_a' 'b' {x='y'} foo='${concat(data.data_a.b.1.<caret>)}'", "count", "x");
  }

  public void testDataSourcePropertyCompletionAfterSelect() {
    doBasicCompletionTest("data 'data_a' 'b' {x='y'} foo='${data.data_a.b[1].<caret>}'", "count", "x");
    doBasicCompletionTest("data 'data_a' 'b' {x='y'} foo='${concat(data.data_a.b[1].<caret>)}'", "count", "x");
  }

  public void testDataSourcePropertyCompletionAfterStar() {
    doBasicCompletionTest("data 'data_a' 'b' {x='y'} foo='${data.data_a.b.*.<caret>}'", "count", "x");
    doBasicCompletionTest("data 'data_a' 'b' {x='y'} foo='${concat(data.data_a.b.*.<caret>)}'", "count", "x");
  }

  public void testDataSourcePropertyCompletionAfterQuotedStar() {
    doBasicCompletionTest("data 'data_a' 'b' {x='y'} foo='${data.data_a.b.\"*\".<caret>}'", "count", "x");
    doBasicCompletionTest("data 'data_a' 'b' {x='y'} foo='${concat(data.data_a.b.\"*\".<caret>)}'", "count", "x");
  }

  public void testDataSourcePropertyCompletionQuotedDataSourceName() {
    doBasicCompletionTest("data 'data_a' 'b' {x='y'} foo='${data.data_a.\"b\".<caret>}'", 0);
    doBasicCompletionTest("data 'data_a' 'b' {x='y'} foo='${concat(data.data_a.\"b\".<caret>)}'", 0);
  }
  //</editor-fold>

  public void testSpecial_HasDynamicAttributes_Property_Not_Advised() {
    doBasicCompletionTest("""
                            data "terraform_remote_state" "x" {}
                            output "a" {
                              value = "${data.terraform_remote_state.x.<caret>}"
                            }""", strings -> {
      then(strings).contains("backend").doesNotContain("__has_dynamic_attributes");
      return true;
    });
  }

  public void testLocalCompletion() {
    doBasicCompletionTest("foo='${<caret>}'", "local");
  }

  public void testLocalCompletionAfter() {
    doBasicCompletionTest("foo='${local.<caret>}'", 0);
  }

  public void testLocalsCompletion() {
    doBasicCompletionTest("locals {x='y'} foo='${local.<caret>}'", "x");
  }

  public void testLocals2Completion() {
    doBasicCompletionTest("locals {x='y' a=true} foo='${local.<caret>}'", "x", "a");
  }

  public void testDefinedComputedBlockCompletion() {
    doBasicCompletionTest("""
                            resource "azurerm_linux_virtual_machine" "example" {
                              name                = "example-machine"
                              resource_group_name = "test-rg"
                              location            = "US West"
                              size                = "Standard_F2"
                              admin_username      = "adminuser"
                              network_interface_ids = [
                                "example-nic"
                              ]
                            
                              admin_ssh_key {
                                username   = "adminuser"
                                public_key = file("~/.ssh/id_rsa.pub")
                              }
                            
                              os_disk {
                                caching              = "ReadWrite"
                                storage_account_type = "Standard_LRS"
                              }
                            
                              source_image_reference {
                                publisher = "Canonical"
                                offer     = "0001-com-ubuntu-server-jammy"
                                sku       = "22_04-lts"
                                version   = "latest"
                              }
                            }
                            
                            output "x" {
                              value = "${azurerm_linux_virtual_machine.example.os_disk.<caret>}"
                            }""", "caching", "storage_account_type");
  }

  public void testDefinedFunctionsCompletion() {
    doBasicCompletionTest(
      "test = '${aws<caret>}'",
      3,
      "provider::aws::arn_build", "provider::aws::arn_parse", "provider::aws::trim_iam_role_path"
    );
  }
}
