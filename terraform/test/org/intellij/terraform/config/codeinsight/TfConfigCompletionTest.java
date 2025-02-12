// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.codeinsight;

import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.util.registry.Registry;
import org.intellij.terraform.config.model.*;

import java.util.*;
import java.util.function.Predicate;
import java.util.stream.Collectors;
import java.util.stream.StreamSupport;

import static org.assertj.core.api.BDDAssertions.then;
import static org.intellij.terraform.config.CompletionTestCase.Matcher.*;

@SuppressWarnings({"ArraysAsListWithZeroOrOneArgument", "RedundantThrows"})
public class TfConfigCompletionTest extends TfBaseCompletionTestCase {

  private static final int ENTRIES_LIST_SIZE = 900; //almost x2 to the default registry value

  private static final Set<ProviderTier> tiers = Set.of(ProviderTier.TIER_BUILTIN, ProviderTier.TIER_OFFICIAL, ProviderTier.TIER_LOCAL);

  @Override
  protected void setUp() throws Exception {
    super.setUp();
    Registry.get("ide.completion.variant.limit").setValue((ENTRIES_LIST_SIZE + 100) * 2, getTestRootDisposable());
  }

  @Override
  protected void tearDown() throws Exception {
    super.tearDown();
  }

  public void testBlockKeywordCompletion() throws Exception {
    doBasicCompletionTest("<caret> {}", TfCompletionUtil.INSTANCE.getRootBlockKeywords());
    doBasicCompletionTest("a=1\n<caret> {}", TfCompletionUtil.INSTANCE.getRootBlockKeywords());

    doBasicCompletionTest("<caret> ", TfCompletionUtil.INSTANCE.getRootBlockKeywords());
    doBasicCompletionTest("a=1\n<caret> ", TfCompletionUtil.INSTANCE.getRootBlockKeywords());

    doBasicCompletionTest("\"<caret>\" {}", TfCompletionUtil.INSTANCE.getRootBlockKeywords());
    doBasicCompletionTest("\"<caret> {}", TfCompletionUtil.INSTANCE.getRootBlockKeywords());
    doBasicCompletionTest("a=1\n\"<caret>\" {}", TfCompletionUtil.INSTANCE.getRootBlockKeywords());
    doBasicCompletionTest("a=1\n\"<caret> {}", TfCompletionUtil.INSTANCE.getRootBlockKeywords());

    doBasicCompletionTest("\"<caret>\" ", TfCompletionUtil.INSTANCE.getRootBlockKeywords());
    doBasicCompletionTest("\"<caret> ", TfCompletionUtil.INSTANCE.getRootBlockKeywords());
    doBasicCompletionTest("a=1\n\"<caret>\" ", TfCompletionUtil.INSTANCE.getRootBlockKeywords());
    doBasicCompletionTest("a=1\n\"<caret> ", TfCompletionUtil.INSTANCE.getRootBlockKeywords());
  }

  public void testNoBlockKeywordCompletion() throws Exception {
    doBasicCompletionTest("a={\n<caret>\n}", 0);
  }

  //<editor-fold desc="Resources completion tests">
  public void testResourceTypeCompletion() throws Exception {
    final TreeSet<String> set = StreamSupport.stream(
        Spliterators.spliteratorUnknownSize(TypeModelProvider.Companion.getGlobalModel().allResources().iterator(), Spliterator.ORDERED),
        false)
      .filter(type -> tiers.contains(type.getProvider().getTier()))
      .map(ResourceType::getType)
      .collect(Collectors.toCollection(TreeSet::new));
    final Predicate<Collection<String>> matcher =
      getPartialMatcher(new ArrayList<>(set).subList(0, Math.min(ENTRIES_LIST_SIZE, set.size())));
    doBasicCompletionTest("resource <caret>", matcher);
    doBasicCompletionTest("resource <caret> {}", matcher);
    doBasicCompletionTest("resource <caret> \"aaa\" {}", matcher);
    doBasicCompletionTest("\"resource\" <caret>", matcher);
    doBasicCompletionTest("\"resource\" <caret> {}", matcher);
    doBasicCompletionTest("\"resource\" <caret> \"aaa\" {}", matcher);
  }

  public void testResourceQuotedTypeCompletion() throws Exception {
    final TreeSet<String> set = StreamSupport.stream(
        Spliterators.spliteratorUnknownSize(TypeModelProvider.Companion.getGlobalModel().allResources().iterator(), Spliterator.ORDERED),
        false)
      .filter(type -> tiers.contains(type.getProvider().getTier()))
      .map(ResourceType::getType)
      .collect(Collectors.toCollection(TreeSet::new));
    final Predicate<Collection<String>> matcher =
      getPartialMatcher(new ArrayList<>(set).subList(0, Math.min(ENTRIES_LIST_SIZE, set.size())));
    doBasicCompletionTest("resource \"<caret>", matcher);
    doBasicCompletionTest("resource '<caret>", matcher);
    doBasicCompletionTest("resource \"<caret>\n{}", matcher);
    doBasicCompletionTest("resource '<caret>\n{}", matcher);
    doBasicCompletionTest("resource \"<caret>\" {}", matcher);
    doBasicCompletionTest("resource \"<caret>\" \"aaa\" {}", matcher);
  }

  public void testResourceQuotedKeywordCompletion() throws Exception {
    final TreeSet<String> set = StreamSupport.stream(
        Spliterators.spliteratorUnknownSize(TypeModelProvider.Companion.getGlobalModel().allResources().iterator(), Spliterator.ORDERED),
        false)
      .filter(type -> tiers.contains(type.getProvider().getTier()))
      .map(ResourceType::getType)
      .collect(Collectors.toCollection(TreeSet::new));
    final Predicate<Collection<String>> matcher =
      getPartialMatcher(new ArrayList<>(set).subList(0, Math.min(ENTRIES_LIST_SIZE, set.size())));
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

  public void testResourceEachValueCompletion() throws Exception {
    doBasicCompletionTest("""
                            resource "aws_instance" "resource-name-test0" {
                              for_each = {"vm1" = { type = "t2.micro", ami = "ami-052efd3df9dad4825", name = "resource-terraform-test0" }}
                              ami           = each.value.ami
                              instance_type = each.value.<caret>
                              tags = {
                                Name = each.value.name
                              }
                            }
                            """, 3, "ami", "name", "type");

    doBasicCompletionTest("""
                            variable "servers" {
                              type = map(object({
                                instance_type = string
                                ami           = string
                              }))
                              default = {
                                web = {
                                  instance_type = "t2.micro"
                                  ami           = "ami-12345678"
                                }
                                app = {
                                  instance_type = "t2.medium"
                                  ami           = "ami-87654321"
                                }
                              }
                            }
                            
                            resource "aws_instance" "example" {
                              for_each = var.servers
                            
                              ami           = each.value.<caret>
                              instance_type = each.value.instance_type
                            }
                            """, 2, "ami", "instance_type");
  }

  public void testResourceCommonPropertyCompletionFromModel() throws Exception {
    final HashSet<String> base = new HashSet<>(COMMON_RESOURCE_PROPERTIES);
    final ResourceType type = TypeModelProvider.Companion.getGlobalModel().getResourceType("azurerm_linux_virtual_machine", null);
    assertNotNull(type);
    for (PropertyOrBlockType it : type.getProperties().values()) {
      if (it.getConfigurable()) base.add(it.getName());
    }
    doBasicCompletionTest("resource azurerm_linux_virtual_machine x {\n<caret>\n}", base);
    doBasicCompletionTest("resource azurerm_linux_virtual_machine x {\n<caret> = \"name\"\n}", "size", "location");
    doBasicCompletionTest("resource azurerm_linux_virtual_machine x {\n<caret> = true\n}", "allow_extension_operations", "bypass_platform_safety_checks_on_user_schedule_enabled");
    doBasicCompletionTest("resource azurerm_linux_virtual_machine x {\n<caret> {}\n}", "additional_capabilities");

    doBasicCompletionTest("resource azurerm_linux_virtual_machine x {\n\"<caret>\"\n}", base);
    doBasicCompletionTest("resource azurerm_linux_virtual_machine x {\n\"<caret>\" = \"name\"\n}", "size", "location");
    doBasicCompletionTest("resource azurerm_linux_virtual_machine x {\n\"<caret>\" = true\n}", "allow_extension_operations", "bypass_platform_safety_checks_on_user_schedule_enabled");
    doBasicCompletionTest("resource azurerm_linux_virtual_machine x {\n\"<caret>\" {}\n}", "additional_capabilities");

    // Should understand interpolation result
    doBasicCompletionTest("resource azurerm_linux_virtual_machine x {\n<caret> = \"${true}\"\n}", strings -> {
      then(strings).contains("allow_extension_operations", "bypass_platform_safety_checks_on_user_schedule_enabled").doesNotContain("additional_capabilities", "size", "location");
      return true;
    });
    // Or not
    doBasicCompletionTest("resource azurerm_linux_virtual_machine x {\n<caret> = \"${}\"\n}", strings -> {
      then(strings).contains("allow_extension_operations", "bypass_platform_safety_checks_on_user_schedule_enabled", "size", "location").doesNotContain("additional_capabilities");
      return true;
    });
  }

  public void testResourceCommonPropertyAlreadyDefinedNotShownAgain() throws Exception {
    final ResourceType type = TypeModelProvider.Companion.getGlobalModel().getResourceType("azurerm_linux_virtual_machine", null);
    assertNotNull(type);

    // Should not add existing props to completion variants
    doBasicCompletionTest("""
                            resource azurerm_linux_virtual_machine x {
                              <caret>  admin_username = ""
                              location = ""
                            }
                            """, not("admin_username", "location"));
    doBasicCompletionTest("""
                            resource azurerm_linux_virtual_machine x {
                              admin_username = ""
                              <caret>  location = ""
                            }
                            """, not("admin_username", "location"));
    doBasicCompletionTest("""
                            resource azurerm_linux_virtual_machine x {
                              admin_username = ""
                              location = ""
                              <caret>}
                            """, not("admin_username", "location"));

    // yet should advice if we stand on it
    doBasicCompletionTest("""
                            resource azurerm_linux_virtual_machine x {
                              admin_username = ""
                              <caret>location = ""
                            }
                            """, and(not("admin_username"), all("location")));
    doBasicCompletionTest("""
                            resource azurerm_linux_virtual_machine x {
                              <caret>admin_username = ""
                              location = ""
                            }
                            """, and(not("location"), all("admin_username")));
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
    final TreeSet<String> set = StreamSupport.stream(
        Spliterators.spliteratorUnknownSize(TypeModelProvider.Companion.getGlobalModel().allResources().iterator(), Spliterator.ORDERED),
        false)
      .filter(type -> tiers.contains(type.getProvider().getTier()))
      .map(ResourceType::getType)
      .collect(Collectors.toCollection(TreeSet::new));
    then(set).contains("template_file", "vault_kv_secret");
    final Predicate<Collection<String>> matcher =
      getPartialMatcher(new ArrayList<>(set).subList(0, Math.min(ENTRIES_LIST_SIZE, set.size())));
    doBasicCompletionTest("provider aws {}\nresource <caret>", matcher);
    doBasicCompletionTest("provider aws {}\nresource <caret> {}", matcher);
    doBasicCompletionTest("provider aws {}\nresource <caret> \"aaa\" {}", matcher);
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
    final TreeSet<String> set = StreamSupport.stream(
        Spliterators.spliteratorUnknownSize(TypeModelProvider.Companion.getGlobalModel().allDatasources().iterator(), Spliterator.ORDERED),
        false)
      .filter(type -> tiers.contains(type.getProvider().getTier()))
      .map(DataSourceType::getType)
      .collect(Collectors.toCollection(TreeSet::new));
    final Predicate<Collection<String>> matcher =
      getPartialMatcher(new ArrayList<>(set).subList(0, Math.min(ENTRIES_LIST_SIZE, set.size())));
    doBasicCompletionTest("data <caret>", matcher);
    doBasicCompletionTest("data <caret> {}", matcher);
    doBasicCompletionTest("data <caret> \"aaa\" {}", matcher);
  }

  public void testCheckBlockCompletion() throws Exception {
    doBasicCompletionTest("check {<caret>}", "assert", "data");
    doBasicCompletionTest(
      """
        check "certificate" {
          assert {
            condition     = aws_acm_certificate.cert.status == "ERRORED"
            error_message = "Certificate status is ${aws_acm_certificate.cert.status}"
          }
          data abc {
            <caret>
          }"
        }""", COMMON_DATA_SOURCE_PROPERTIES);
    doTheOnlyVariantCompletionTest(
      """
        check "certificate" {
          dat<caret>
        }""",
      """
        check "certificate" {
          data "" "" {}
        }""", false
    );
  }

  public void testRemovedBlockCompletion() throws Exception {
    doBasicCompletionTest("removed {<caret>}", 2, "from", "lifecycle");
    doBasicCompletionTest(
      """
        removed {
          from = test
          lifecycle {
            <caret>
          }
        }""", 6, "postcondition", "precondition", "create_before_destroy", "ignore_changes",
      "prevent_destroy", "replace_triggered_by");
  }

  public void testDataSourceQuotedTypeCompletion() throws Exception {
    final TreeSet<String> set = StreamSupport.stream(
        Spliterators.spliteratorUnknownSize(TypeModelProvider.Companion.getGlobalModel().allDatasources().iterator(), Spliterator.ORDERED),
        false)
      .filter(type -> tiers.contains(type.getProvider().getTier()))
      .map(DataSourceType::getType)
      .collect(Collectors.toCollection(TreeSet::new));
    final Predicate<Collection<String>> matcher =
      getPartialMatcher(new ArrayList<>(set).subList(0, Math.min(ENTRIES_LIST_SIZE, set.size())));
    doBasicCompletionTest("data \"<caret>", matcher);
    doBasicCompletionTest("data '<caret>", matcher);
    doBasicCompletionTest("data \"<caret>\n{}", matcher);
    doBasicCompletionTest("data '<caret>\n{}", matcher);
    doBasicCompletionTest("data \"<caret>\" {}", matcher);
    doBasicCompletionTest("data \"<caret>\" \"aaa\" {}", matcher);
  }

  public void testDataSourceQuotedKeywordCompletion() throws Exception {
    final TreeSet<String> set = StreamSupport.stream(
        Spliterators.spliteratorUnknownSize(TypeModelProvider.Companion.getGlobalModel().allDatasources().iterator(), Spliterator.ORDERED),
        false)
      .filter(type -> tiers.contains(type.getProvider().getTier()))
      .map(DataSourceType::getType)
      .collect(Collectors.toCollection(TreeSet::new));
    final Predicate<Collection<String>> matcher =
      getPartialMatcher(new ArrayList<>(set).subList(0, Math.min(ENTRIES_LIST_SIZE, set.size())));
    doBasicCompletionTest("\"data\" \"<caret>", matcher);
    doBasicCompletionTest("\"data\" '<caret>", matcher);
    doBasicCompletionTest("\"data\" \"<caret>\n{}", matcher);
    doBasicCompletionTest("\"data\" '<caret>\n{}", matcher);
    doBasicCompletionTest("\"data\" \"<caret>\" {}", matcher);
    doBasicCompletionTest("\"data\" \"<caret>\" \"aaa\" {}", matcher);
  }


  public void testDataSourceCommonPropertyCompletion() throws Exception {
    doBasicCompletionTest("data abc {\n<caret>\n}", COMMON_DATA_SOURCE_PROPERTIES);
    final HashSet<String> set = new HashSet<>(COMMON_DATA_SOURCE_PROPERTIES);
    set.remove("id");
    doBasicCompletionTest("data \"x\" {\nid='a'\n<caret>\n}", set);
    doBasicCompletionTest("data abc {\n<caret> = true\n}", Collections.emptySet());
    // lifecycle block for DataSource, that's why size=1
    doBasicCompletionTest("data abc {\n<caret> {}\n}", 1, "lifecycle");
  }

  public void testDataSourceCommonPropertyCompletionFromModel() throws Exception {
    final HashSet<String> base = new HashSet<>(COMMON_DATA_SOURCE_PROPERTIES);
    final DataSourceType type = TypeModelProvider.Companion.getGlobalModel().getDataSourceType("azurerm_kubernetes_cluster_node_pool", null);
    assertNotNull(type);
    for (PropertyOrBlockType it : type.getProperties().values()) {
      if (it.getConfigurable()) base.add(it.getName());
    }
    doBasicCompletionTest("data azurerm_kubernetes_cluster_node_pool x {\n<caret>\n}", base);
    doBasicCompletionTest("data azurerm_kubernetes_cluster_node_pool x {\n<caret> = \"name\"\n}",
                          "kubernetes_cluster_name",
                          "resource_group_name",
                          "provider"
    );
    doBasicCompletionTest("data azurerm_storage_account_blob_container_sas x {\n<caret> = true\n}", "https_only");
    doBasicCompletionTest("data azurerm_storage_account_blob_container_sas x {\n<caret> {}\n}", "lifecycle", "permissions", "timeouts");

    // Should understand interpolation result
    doBasicCompletionTest("data  azurerm_storage_account_sas x {\n<caret> = \"${true}\"\n}", strings -> {
      then(strings).contains("https_only").doesNotContain("connection_string", "services");
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
    final TreeSet<String> set = StreamSupport.stream(
        Spliterators.spliteratorUnknownSize(TypeModelProvider.Companion.getGlobalModel().allDatasources().iterator(), Spliterator.ORDERED),
        false)
      .filter(type -> tiers.contains(type.getProvider().getTier()))
      .map(DataSourceType::getType)
      .collect(Collectors.toCollection(TreeSet::new));
    then(set).contains("template_file", "vault_kv_secret");
    final Predicate<Collection<String>> matcher =
      getPartialMatcher(new ArrayList<>(set).subList(0, Math.min(ENTRIES_LIST_SIZE, set.size())));
    doBasicCompletionTest("provider aws {}\ndata <caret>", matcher);
    doBasicCompletionTest("provider aws {}\ndata <caret> {}", matcher);
    doBasicCompletionTest("provider aws {}\ndata <caret> \"aaa\" {}", matcher);
  }
  //</editor-fold>

  public void testOutputBasicCompletion() throws Exception {
    doBasicCompletionTest("output test1 {<caret>}", 5, "description");
    doBasicCompletionTest("output test2 {\np<caret>}", 3, "precondition", "description", "depends_on");
  }

  public void testVariableBasicCompletion() throws Exception {
    doBasicCompletionTest("variable test1 {\n<caret>}", 6, "type");
    doBasicCompletionTest("variable test2 {\ns<caret>}", 2, "sensitive", "description");
    doBasicCompletionTest("variable test3 {\nn<caret>}", 4, "nullable", "validation");
    doBasicCompletionTest("variable test4 {\nd<caret>}", 3, "default");
  }

  public void testLifecycleBasicCompletion() throws Exception {
    doBasicCompletionTest("""
                            resource null_resource test {
                              lifecycle {
                                con<caret>
                              }
                            }
                            """.trim(), 2, "precondition", "postcondition");

    doBasicCompletionTest("""
                            data "abbey_identity" "test" {
                              id = ""
                              lifecycle {
                                <caret>
                              }
                            }
                            """.trim(), 6, "replace_triggered_by");

    doBasicCompletionTest("""
                            resource null_resource test {
                              lifecycle {
                                create_before_destroy = f<caret>
                              }
                            }
                            """.trim(), "false");
  }

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
    doBasicCompletionTest("""
                            variable "obj-var" {
                               type = object({
                                 var1 = string
                                 var2 = list(string)
                                 var3 = object({
                                   var4 = object({
                                     var6 = list(any)
                                     var7 = list(bool)
                                     var8 = list(number)
                                   })
                                 })
                               })
                             }
                            
                             module "test" {
                               source = "./"
                            
                               obj-var = {
                                 var1 = ""
                                 var2 = []
                                 var3 = {
                                   var4 = {
                                    <caret>
                                   }
                                 }
                               }
                             }
                            """, Arrays.asList("var6", "var7", "var8"));
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
    doBasicCompletionTest("module 'x' { id = <caret>}", not("each"));
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

  public void testCompleteResourceFromAnotherModuleInImportBlock() {
    myFixture.addFileToProject("submodule/sub.tf", """
      resource "MyType" "MyName" {}
      """);
    myFixture.configureByText("main.tf", """
      import {
        id = "terraform"
        to = module.submodule.<caret>
      }
      
      module "submodule" {
        source = "./submodule"
      }
      """);
    myFixture.testCompletionVariants("main.tf", "MyType.MyName");
  }

  public void testCompleteResourceFromMovedBlock() {
    myFixture.addFileToProject("modules/compute/main.tf", """ 
      resource "aws_instance" "example1" { }
      
      resource "aws_instance" "example" { }
      """);
    myFixture.configureByText("main.tf", """
      module "ec2_instance" {
        source         = "./modules/compute"
        security_group = module.web_security_group.security_group_id
        public_subnets = module.vpc.public_subnets
      }
      
      moved {
        from = aws_instance.example
        to = module.ec2_instance.aws<caret>
      }
      """);
    myFixture.testCompletionVariants("main.tf", "aws_instance.example", "aws_instance.example1");
  }

  public void testOfficialResourcesVariants() {
    myFixture.configureByText("main.tf", """ 
      resource "aws_ec2_host<caret>"
      """);
    LookupElement[] lookupElements = myFixture.complete(CompletionType.BASIC, 1);
    assertEquals(2, lookupElements.length);
    Set<String> lookupStrings = Arrays.stream(lookupElements).map(el -> {
      ResourceType resourceType = (ResourceType)el.getObject();
      String name = resourceType.getType();
      String provider = resourceType.getProvider().getFullName();
      return "%s %s".formatted(name, provider);
    }).collect(Collectors.toSet());
    assertEquals(lookupStrings, Set.of("aws_ec2_host hashicorp/aws", "awscc_ec2_host hashicorp/awscc"));
  }

  public void testAllResourcesVariants() {
    myFixture.configureByText("main.tf", """ 
      resource "aws_ec2_host<caret>"
      """);
    LookupElement[] lookupElements = myFixture.complete(CompletionType.BASIC, 2);
    assertEquals(4, lookupElements.length);
    Set<String> lookupStrings = Arrays.stream(lookupElements).map(el -> {
      ResourceType resourceType = (ResourceType)el.getObject();
      String name = resourceType.getType();
      String provider = resourceType.getProvider().getFullName();
      return "%s %s".formatted(name, provider);
    }).collect(Collectors.toSet());
    assertEquals(lookupStrings, Set.of("aws_ec2_host jandillenkofer/aws","aws_ec2_host hashicorp/aws", "aws_ec2_host msalman899/aws", "awscc_ec2_host hashicorp/awscc"));
  }

  public void testTerraformBlockCompletion() {
    myFixture.configureByText("main.tf", """ 
      terraform {
        <caret>
      }
      """);
    myFixture.testCompletionVariants("main.tf", "backend", "cloud", "experiments", "required_providers", "required_version");
  }

  public void testTerraformBlockCompletionBackend() {
    myFixture.configureByText("main.tf", """ 
      terraform {
        backend "<caret>" {}
      }
      """);
    TypeModel model = TypeModelProvider.getGlobalModel();
    String[] backends = model.getBackends().stream().map(e -> e.getType()).toArray(String[]::new);
    myFixture.testCompletionVariants("main.tf", backends);
  }

  public void testProviderFunctionsCompletion() {
    doBasicCompletionTest(
      """
      resource "kubernetes_manifest" "example" {
        manifest = manifest_d<caret>
      }
      """, 2, "provider::kubernetes::manifest_decode", "provider::kubernetes::manifest_decode_multi"
    );

    doBasicCompletionTest(
      """
      locals {
        tfvars = decode_tf<caret>
      }
      """, 1, "provider::terraform::decode_tfvars"
    );

    doBasicCompletionTest(
      """
      resource "some_resource" "name" {
        enabled = assert<caret>
      }
      """, "provider::assert::true"
    );
  }

}