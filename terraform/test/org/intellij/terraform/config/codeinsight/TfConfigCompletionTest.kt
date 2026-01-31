// Copyright 2000-2026 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.intellij.terraform.config.codeinsight

import com.intellij.codeInsight.completion.CompletionType
import com.intellij.openapi.util.registry.Registry
import org.intellij.terraform.config.codeinsight.TfCompletionUtil.RootBlockKeywords
import org.intellij.terraform.config.model.DataSourceType
import org.intellij.terraform.config.model.EphemeralType
import org.intellij.terraform.config.model.ProviderTier
import org.intellij.terraform.config.model.ResourceOrDataSourceType
import org.intellij.terraform.config.model.ResourceType
import org.intellij.terraform.config.model.TypeModelProvider.Companion.globalModel
import org.intellij.terraform.terragrunt.TerragruntCompletionTest

internal class TfConfigCompletionTest : TfBaseCompletionTestCase() {

  override fun setUp() {
    super.setUp()
    Registry.get("ide.completion.variant.limit").setValue(COMPLETION_VARIANTS_LIMIT, testRootDisposable)
  }

  override fun tearDown() {
    super.tearDown()
  }

  fun testBlockKeywordCompletion() {
    doBasicCompletionTest("<caret> {}", RootBlockKeywords)
    doBasicCompletionTest("a=1\n<caret> {}", RootBlockKeywords)

    doBasicCompletionTest("<caret> ", RootBlockKeywords)
    doBasicCompletionTest("a=1\n<caret> ", RootBlockKeywords)

    doBasicCompletionTest("\"<caret>\" {}", RootBlockKeywords)
    doBasicCompletionTest("\"<caret> {}", RootBlockKeywords)
    doBasicCompletionTest("a=1\n\"<caret>\" {}", RootBlockKeywords)
    doBasicCompletionTest("a=1\n\"<caret> {}", RootBlockKeywords)

    doBasicCompletionTest("\"<caret>\" ", RootBlockKeywords)
    doBasicCompletionTest("\"<caret> ", RootBlockKeywords)
    doBasicCompletionTest("a=1\n\"<caret>\" ", RootBlockKeywords)
    doBasicCompletionTest("a=1\n\"<caret> ", RootBlockKeywords)
  }

  fun testNoBlockKeywordCompletion() {
    doBasicCompletionTest("a={\n<caret>\n}", 0)
  }

  fun testNotAllowedRootBlockInTerraform() {
    val file = myFixture.configureByText("test.tf", "<caret>")
    val completionVariants = myFixture.getCompletionVariants(file.virtualFile.name)
      ?.filterNot { it == "terraform" || it == "locals" }
      .orEmpty()
    assertNotEmpty(completionVariants)

    val unexpectedTerragruntBlocks = TerragruntCompletionTest.TerragruntBlockKeywords.filter { it in completionVariants }
    assertTrue(
      "These Terragrunt-only root blocks should not appear in a Terraform file: $unexpectedTerragruntBlocks",
      unexpectedTerragruntBlocks.isEmpty()
    )

    val unexpectedStackBlocks = TerragruntCompletionTest.StackBlockKeywords.filter { it in completionVariants }
    assertTrue(
      "These Terragrunt Stack-only root blocks should not appear in a Terraform file: $unexpectedStackBlocks",
      unexpectedStackBlocks.isEmpty()
    )
  }

  fun testResourceTypeCompletion() {
    val matcher = getPartialMatcher(collectTypeNames<ResourceType>())

    doBasicCompletionTest("resource <caret>", matcher)
    doBasicCompletionTest("resource <caret> {}", matcher)
    doBasicCompletionTest("resource <caret> \"aaa\" {}", matcher)
    doBasicCompletionTest("\"resource\" <caret>", matcher)
    doBasicCompletionTest("\"resource\" <caret> {}", matcher)
    doBasicCompletionTest("\"resource\" <caret> \"aaa\" {}", matcher)
  }

  private inline fun <reified T : ResourceOrDataSourceType> collectTypeNames(): List<String> {
    val items = when (T::class) {
                  ResourceType::class -> globalModel.allResources()
                  EphemeralType::class -> globalModel.allEphemeralResources()
                  DataSourceType::class -> globalModel.allDataSources()
                  else -> null
                } ?: return emptyList()

    return items.filter { it.provider.tier in ProviderTier.PreferedProviders }
      .map { it.type }
      .sorted()
      .take(ENTRIES_LIST_SIZE)
      .toList()
  }

  fun testResourceQuotedTypeCompletion() {
    val matcher = getPartialMatcher(collectTypeNames<ResourceType>())

    doBasicCompletionTest("resource \"<caret>", matcher)
    doBasicCompletionTest("resource '<caret>", matcher)
    doBasicCompletionTest("resource \"<caret>\n{}", matcher)
    doBasicCompletionTest("resource '<caret>\n{}", matcher)
    doBasicCompletionTest("resource \"<caret>\" {}", matcher)
    doBasicCompletionTest("resource \"<caret>\" \"aaa\" {}", matcher)
  }

  fun testResourceQuotedKeywordCompletion() {
    val matcher = getPartialMatcher(collectTypeNames<ResourceType>())

    doBasicCompletionTest("\"resource\" \"<caret>", matcher)
    doBasicCompletionTest("\"resource\" '<caret>", matcher)
    doBasicCompletionTest("\"resource\" \"<caret>\n{}", matcher)
    doBasicCompletionTest("\"resource\" '<caret>\n{}", matcher)
    doBasicCompletionTest("\"resource\" \"<caret>\" {}", matcher)
    doBasicCompletionTest("\"resource\" \"<caret>\" \"aaa\" {}", matcher)
  }

  fun testResourceCommonPropertyCompletion() {
    doBasicCompletionTest("resource abc {\n<caret>\n}", commonResourceProperties)

    val propertiesWithoutId = commonResourceProperties.toMutableList().apply { remove("id") }
    doBasicCompletionTest("resource \"x\" {\nid='a'\n<caret>\n}", propertiesWithoutId)
    doBasicCompletionTest("resource abc {\n<caret> = true\n}", emptyList())
    doBasicCompletionTest("resource abc {\n<caret> {}\n}", listOf("lifecycle", "connection", "provisioner", "dynamic"))

    doBasicCompletionTest(
      "resource abc {\n<caret>lifecycle {}\n}",
      listOf("connection", "dynamic", "lifecycle", "provisioner")
    )
  }

  fun testResourceDynamicCompletion() {
    doBasicCompletionTest("resource abc {\n dynamic x {<caret>}\n}", "for_each", "labels", "iterator", "content")
    doBasicCompletionTest("resource abc {\n dynamic <caret> \n}", Matcher.not("lifecycle", "provisioner", "dynamic"))
    doBasicCompletionTest("resource abc {\n dynamic <caret> {}\n}", Matcher.not("lifecycle", "provisioner", "dynamic"))
  }

  fun testResourceForEachCompletion() {
    doBasicCompletionTest("resource 'null_resource' 'x' { id = <caret>}", Matcher.not("each"))
    doBasicCompletionTest("resource 'null_resource' 'x' { for_each={}\n id = <caret>}", "each")
    doBasicCompletionTest("resource 'null_resource' 'x' { for_each={}\n id = each.<caret>}", 2, "key", "value")
  }

  fun testResourceEachValueCompletion() {
    doBasicCompletionTest(
      """
        resource "aws_instance" "resource-name-test0" {
          for_each = {"vm1" = { type = "t2.micro", ami = "ami-052efd3df9dad4825", name = "resource-terraform-test0" }}
          ami           = each.value.ami
          instance_type = each.value.<caret>
          tags = {
            Name = each.value.name
          }
        }
        """.trimIndent(), 3, "ami", "name", "type")

    doBasicCompletionTest(
      """
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
        """.trimIndent(), 2, "ami", "instance_type")
  }

  fun testResourceCommonPropertyCompletionFromModel() {
    val properties = commonResourceProperties.toHashSet()

    val azureResource = globalModel.getResourceType("azurerm_linux_virtual_machine", null)
    assertNotNull(azureResource)
    azureResource?.properties?.values?.filter { it.configurable }?.forEach { properties.add(it.name) }

    doBasicCompletionTest("resource azurerm_linux_virtual_machine x {\n<caret>\n}", properties)
    doBasicCompletionTest("resource azurerm_linux_virtual_machine x {\n<caret> = \"name\"\n}", "size", "location")
    doBasicCompletionTest("resource azurerm_linux_virtual_machine x {\n<caret> = true\n}", "allow_extension_operations",
                          "bypass_platform_safety_checks_on_user_schedule_enabled")
    doBasicCompletionTest("resource azurerm_linux_virtual_machine x {\n<caret> {}\n}", "additional_capabilities")

    //doBasicCompletionTest("resource azurerm_linux_virtual_machine x {\n\"<caret>\"\n}", properties)
    doBasicCompletionTest("resource azurerm_linux_virtual_machine x {\n\"<caret>\" = \"name\"\n}", "size", "location")
    doBasicCompletionTest("resource azurerm_linux_virtual_machine x {\n\"<caret>\" = true\n}", "allow_extension_operations",
                          "bypass_platform_safety_checks_on_user_schedule_enabled")
    doBasicCompletionTest("resource azurerm_linux_virtual_machine x {\n\"<caret>\" {}\n}", "additional_capabilities")

    // Should understand a interpolation result
    doBasicCompletionTest($$"resource azurerm_linux_virtual_machine x {\n<caret> = \"${true}\"\n}") { items ->
      items.containsAll(listOf("allow_extension_operations", "bypass_platform_safety_checks_on_user_schedule_enabled")) &&
      listOf("additional_capabilities", "size", "location").none { it in items }
    }
    // Or not
    doBasicCompletionTest($$"resource azurerm_linux_virtual_machine x {\n<caret> = \"${}\"\n}") { items ->
      items.containsAll(listOf("allow_extension_operations", "bypass_platform_safety_checks_on_user_schedule_enabled", "size", "location")) &&
      "additional_capabilities" !in items
    }
  }

  fun testResourceCommonPropertyAlreadyDefinedNotShownAgain() {
    val type = globalModel.getResourceType("azurerm_linux_virtual_machine", null)
    assertNotNull(type)

    // Should not add existing props to completion variants
    doBasicCompletionTest(
      """
        resource azurerm_linux_virtual_machine x {
          <caret>  admin_username = ""
          location = ""
        }
        """.trimIndent(), Matcher.not("admin_username", "location"))
    doBasicCompletionTest(
      """
        resource azurerm_linux_virtual_machine x {
          admin_username = ""
          <caret>  location = ""
        }
        """.trimIndent(), Matcher.not("admin_username", "location"))
    doBasicCompletionTest(
      """
        resource azurerm_linux_virtual_machine x {
          admin_username = ""
          location = ""
          <caret>
        }
        """.trimIndent(), Matcher.not("admin_username", "location"))

    // yet should advice if we stand on it
    doBasicCompletionTest(
      """
        resource azurerm_linux_virtual_machine x {
          admin_username = ""
          <caret>location = ""
        }""".trimIndent(), Matcher.not("admin_username", "location"))
    doBasicCompletionTest("""
      resource azurerm_linux_virtual_machine x {
        <caret>admin_username = ""
        location = ""
      }""".trimIndent(), Matcher.not("location", "admin_username"))
  }

  fun testResourceProviderCompletionFromModel() {
    doBasicCompletionTest("provider Z {}\nresource a b {provider=<caret>}", "Z")
    doBasicCompletionTest("provider Z {}\nresource a b {provider='<caret>'}", "Z")
    doBasicCompletionTest("provider Z {}\nresource a b {provider=\"<caret>\"}", "Z")
    doBasicCompletionTest("provider Z {alias='Y'}\nresource a b {provider=<caret>}", "Z.Y")
    doBasicCompletionTest("provider Z {alias='Y'}\nresource a b {provider='<caret>'}", "Z.Y")
    doBasicCompletionTest("provider Z {alias='Y'}\nresource a b {provider=\"<caret>\"}", "Z.Y")
  }

  fun testResourcePropertyCompletionBeforeInnerBlock() {
    doBasicCompletionTest("resource abc {\n<caret>\nlifecycle {}\n}", commonResourceProperties)

    val propertiesWithoutId = commonResourceProperties.toMutableList().apply { remove("id") }
    doBasicCompletionTest("resource \"x\" {\nid='a'\n<caret>\nlifecycle {}\n}", propertiesWithoutId)
    doBasicCompletionTest("resource abc {\n<caret> = true\nlifecycle {}\n}", emptyList())
  }

  fun testResourceDependsOnCompletion() {
    doBasicCompletionTest("resource x y {}\nresource a b {depends_on=['<caret>']}", 1, "x.y")
    doBasicCompletionTest("resource x y {}\nresource a b {depends_on=[\"<caret>\"]}", 1, "x.y")
    doBasicCompletionTest("data x y {}\nresource a b {depends_on=['<caret>']}", 1, "data.x.y")
    doBasicCompletionTest("data x y {}\nresource a b {depends_on=[\"<caret>\"]}", 1, "data.x.y")

    doBasicCompletionTest("resource x y {}\nresource a b {depends_on=[<caret>]}", 1, "x.y")
    doBasicCompletionTest("data x y {}\nresource a b {depends_on=[<caret>]}", 1, "data.x.y")

    doBasicCompletionTest("variable v{}\nresource x y {}\nresource a b {depends_on=[<caret>]}", 2, "x.y", "var.v")
    doBasicCompletionTest("variable v{}\ndata x y {}\nresource a b {depends_on=[<caret>]}", 2, "data.x.y", "var.v")
  }


  fun testResourceTypeCompletionGivenDefinedProvidersOrForNoPropsProviders() {
    containsResourceTypes("template_file", "vault_kv_secret")

    val matcher = getPartialMatcher(collectTypeNames<ResourceType>())
    doBasicCompletionTest("provider aws {}\nresource <caret>", matcher)
    doBasicCompletionTest("provider aws {}\nresource <caret> {}", matcher)
    doBasicCompletionTest("provider aws {}\nresource <caret> \"aaa\" {}", matcher)
  }

  private fun containsResourceTypes(vararg elements: String): Boolean = globalModel.allResources()
    .filter { it.provider.tier in ProviderTier.PreferedProviders }
    .map { it.type }
    .toList()
    .containsAll(elements.toList())

  fun testResourceNonConfigurablePropertyIsNotAdvised() {
    doBasicCompletionTest("resource \"random_string\" \"x\" { <caret> }") {
      "result" !in it
    }
  }

  //</editor-fold>
  //<editor-fold desc="Data Sources completion tests">

  fun testDataSourceTypeCompletion() {
    val matcher = getPartialMatcher(collectTypeNames<DataSourceType>())
    doBasicCompletionTest("data <caret>", matcher)
    doBasicCompletionTest("data <caret> {}", matcher)
    doBasicCompletionTest("data <caret> \"aaa\" {}", matcher)
  }

  fun testCheckBlockCompletion() {
    doBasicCompletionTest("check {<caret>}", "assert", "data")
    doBasicCompletionTest(
      $$"""
        check "certificate" {
          assert {
            condition     = aws_acm_certificate.cert.status == "ERRORED"
            error_message = "Certificate status is ${aws_acm_certificate.cert.status}"
          }
          data abc {
            <caret>
          }"
        }
        """.trimIndent(), commonDataSourceProperties)
    doTheOnlyVariantCompletionTest(
      """
        check "certificate" {
          dat<caret>
        }
        """.trimIndent(),
      """
        check "certificate" {
          data "" "" {}
        }
        """.trimIndent(), false
    )
  }

  fun testRemovedBlockCompletion() {
    doBasicCompletionTest("removed {<caret>}", 2, "from", "lifecycle")
    doBasicCompletionTest(
      """
        removed {
          from = test
          lifecycle {
            <caret>
          }
        }
        """.trimIndent(), 6, "postcondition", "precondition", "create_before_destroy", "ignore_changes",
      "prevent_destroy", "replace_triggered_by")
  }

  fun testDataSourceQuotedTypeCompletion() {
    val matcher = getPartialMatcher(collectTypeNames<DataSourceType>())

    doBasicCompletionTest("data \"<caret>", matcher)
    doBasicCompletionTest("data '<caret>", matcher)
    doBasicCompletionTest("data \"<caret>\n{}", matcher)
    doBasicCompletionTest("data '<caret>\n{}", matcher)
    doBasicCompletionTest("data \"<caret>\" {}", matcher)
    doBasicCompletionTest("data \"<caret>\" \"aaa\" {}", matcher)
  }

  fun testDataSourceQuotedKeywordCompletion() {
    val matcher = getPartialMatcher(collectTypeNames<DataSourceType>())

    doBasicCompletionTest("\"data\" \"<caret>", matcher)
    doBasicCompletionTest("\"data\" '<caret>", matcher)
    doBasicCompletionTest("\"data\" \"<caret>\n{}", matcher)
    doBasicCompletionTest("\"data\" '<caret>\n{}", matcher)
    doBasicCompletionTest("\"data\" \"<caret>\" {}", matcher)
    doBasicCompletionTest("\"data\" \"<caret>\" \"aaa\" {}", matcher)
  }

  fun testDataSourceCommonPropertyCompletion() {
    doBasicCompletionTest("data abc {\n<caret>\n}", commonDataSourceProperties)
    val propertiesWithoutId = commonDataSourceProperties.toHashSet().apply { remove("id") }
    doBasicCompletionTest("data \"x\" {\nid='a'\n<caret>\n}", propertiesWithoutId)
    doBasicCompletionTest("data abc {\n<caret> = true\n}", emptyList())

    // lifecycle block for DataSource, that's why size=1
    doBasicCompletionTest("data abc {\n<caret> {}\n}", 1, "lifecycle")
  }

  fun testDataSourceCommonPropertyCompletionFromModel() {
    val properties = commonDataSourceProperties.toHashSet()
    val azureDataSource = globalModel.getDataSourceType("azurerm_kubernetes_cluster_node_pool", null)
    assertNotNull(azureDataSource)
    azureDataSource?.properties?.values?.filter { it.configurable }?.forEach { properties.add(it.name) }

    doBasicCompletionTest("data azurerm_kubernetes_cluster_node_pool x {\n<caret>\n}", properties)
    doBasicCompletionTest("data azurerm_kubernetes_cluster_node_pool x {\n<caret> = \"name\"\n}",
                          "kubernetes_cluster_name",
                          "resource_group_name",
                          "provider"
    )
    doBasicCompletionTest("data azurerm_storage_account_blob_container_sas x {\n<caret> = true\n}", "https_only")
    doBasicCompletionTest("data azurerm_storage_account_blob_container_sas x {\n<caret> {}\n}", "lifecycle", "permissions", "timeouts")

    // Should understand interpolation result
    doBasicCompletionTest($$"data  azurerm_storage_account_sas x {\n<caret> = \"${true}\"\n}") { items ->
      items.contains("https_only") && listOf("connection_string", "services").none { it in items }
    }
  }

  fun testDataSourceProviderCompletionFromModel() {
    doBasicCompletionTest("provider Z {}\ndata a b {provider=<caret>}", "Z")
    doBasicCompletionTest("provider Z {}\ndata a b {provider='<caret>'}", "Z")
    doBasicCompletionTest("provider Z {}\ndata a b {provider=\"<caret>\"}", "Z")
    doBasicCompletionTest("provider Z {alias='Y'}\ndata a b {provider=<caret>}", "Z.Y")
    doBasicCompletionTest("provider Z {alias='Y'}\ndata a b {provider='<caret>'}", "Z.Y")
    doBasicCompletionTest("provider Z {alias='Y'}\ndata a b {provider=\"<caret>\"}", "Z.Y")
  }

  fun testDataSourceDependsOnCompletion() {
    doBasicCompletionTest("resource x y {}\ndata a b {depends_on=['<caret>']}", 1, "x.y")
    doBasicCompletionTest("resource x y {}\ndata a b {depends_on=[\"<caret>\"]}", 1, "x.y")
    doBasicCompletionTest("data x y {}\ndata a b {depends_on=['<caret>']}", 1, "data.x.y")
    doBasicCompletionTest("data x y {}\ndata a b {depends_on=[\"<caret>\"]}", 1, "data.x.y")

    doBasicCompletionTest("resource x y {}\ndata a b {depends_on=[<caret>]}", 1, "x.y")
    doBasicCompletionTest("data x y {}\ndata a b {depends_on=[<caret>]}", 1, "data.x.y")

    doBasicCompletionTest("variable v{}\nresource x y {}\ndata a b {depends_on=[<caret>]}", 2, "x.y", "var.v")
    doBasicCompletionTest("variable v{}\ndata x y {}\ndata a b {depends_on=[<caret>]}", 2, "data.x.y", "var.v")
  }

  fun testDataSourceTypeCompletionGivenDefinedProviders() {
    val dataSources = collectTypeNames<DataSourceType>()
    dataSources.containsAll(listOf("template_file", "vault_kv_secret"))

    val matcher = getPartialMatcher(dataSources)
    doBasicCompletionTest("provider aws {}\ndata <caret>", matcher)
    doBasicCompletionTest("provider aws {}\ndata <caret> {}", matcher)
    doBasicCompletionTest("provider aws {}\ndata <caret> \"aaa\" {}", matcher)
  }

  fun testOutputBasicCompletion() {
    doBasicCompletionTest("output test1 {<caret>}", 6, "value", "ephemeral", "sensitive")
    doBasicCompletionTest("output test2 {\np<caret>}", 4, "precondition", "description", "depends_on", "ephemeral")
  }

  fun testVariableBasicCompletion() {
    doBasicCompletionTest("variable test1 {\n<caret>}", 7, "type")
    doBasicCompletionTest("variable test2 {\ns<caret>}", 2, "sensitive", "description")
    doBasicCompletionTest("variable test3 {\nn<caret>}", 4, "nullable", "validation")
    doBasicCompletionTest("variable test4 {\nd<caret>}", 3, "default")
    doBasicCompletionTest(
      "variable test5 {\ne<caret>}", 6, "ephemeral", "type", "sensitive", "description", "nullable", "default"
    )
  }

  fun testLifecycleBasicCompletion() {
    doBasicCompletionTest(
      """
        resource null_resource test {
          lifecycle {
            con<caret>
          }
        }
        """.trimIndent(), 2, "precondition", "postcondition")

    doBasicCompletionTest(
      """
        data "abbey_identity" "test" {
          id = ""
          lifecycle {
            <caret>
          }
        }
        """.trimIndent(), 6, "replace_triggered_by")

    doBasicCompletionTest(
      """
        resource null_resource test {
          lifecycle {
            create_before_destroy = f<caret>
          }
        }
        """.trimIndent(), "false")
  }

  fun testOutputDependsOnCompletion() {
    doBasicCompletionTest("output o {<caret>}", "depends_on")

    doBasicCompletionTest("resource x y {}\noutput o {depends_on=[<caret>]}", 1, "x.y")
    doBasicCompletionTest("resource x y {}\noutput o {depends_on=['<caret>']}", 1, "x.y")
    doBasicCompletionTest("resource x y {}\noutput o {depends_on=[\"<caret>\"]}", 1, "x.y")
    doBasicCompletionTest("data x y {}\noutput o {depends_on=[<caret>]}", 1, "data.x.y")
    doBasicCompletionTest("data x y {}\noutput o {depends_on=['<caret>']}", 1, "data.x.y")
    doBasicCompletionTest("data x y {}\noutput o {depends_on=[\"<caret>\"]}", 1, "data.x.y")

    doBasicCompletionTest("variable v{}\nresource x y {}\noutput o {depends_on=[<caret>]}", 2, "x.y", "var.v")
    doBasicCompletionTest("variable v{}\ndata x y {}\noutput o {depends_on=[<caret>]}", 2, "data.x.y", "var.v")
  }

  fun testVariableTypeCompletion() {
    myCompleteInvocationCount = 1 // Ensure there would not be 'null', 'true' and 'false' variants
    doBasicCompletionTest("variable v { type = <caret> }", 9,
                          "any", "string", "number", "bool", "list", "set", "map", "object", "tuple")
    doBasicCompletionTest("variable v { type = object(x=<caret>) }", 10,
                          "any", "string", "number", "bool", "list", "set", "map", "object", "tuple", "optional")
    doBasicCompletionTest("variable v { type = object(x=optional(<caret>)) }", 9,
                          "any", "string", "number", "bool", "list", "set", "map", "object", "tuple")
    doBasicCompletionTest("variable v { type = object(x=list(<caret>)) }", 9,
                          "any", "string", "number", "bool", "list", "set", "map", "object", "tuple")

    doBasicCompletionTest("variable v { type = object(x=<caret>optional()) }", 10,
                          "any", "string", "number", "bool", "list", "set", "map", "object", "tuple", "optional")
  }

  fun testSpecialHasDynamicAttributesPropertyNotAdvised() {
    doBasicCompletionTest("data \"terraform_remote_state\" \"x\" { <caret> }") { items ->
      items.contains("backend") && "__has_dynamic_attributes" !in items
    }
  }

  fun testModuleProvidersPropertyCompletion() {
    myFixture.addFileToProject("module/a.tf", "provider aws {}\nprovider aws {alias=\"second\"}")
    // via TfPropertyObjectKeyCompletionProvider
    doBasicCompletionTest(
      """
        module x {
          source = "./module/"
          providers = {
            <caret> 
          }
        }
        """.trimIndent(), listOf("aws", "aws.second"))
    doBasicCompletionTest(
      """
        module x {
          source = "./module/"
          providers = {
            "<caret>" 
          }
        }
        """.trimIndent(), listOf("aws", "aws.second"))
    doBasicCompletionTest(
      """
        module x {
          source = "./module/"
          providers = {
            a<caret> 
          }
        }
        """.trimIndent(), listOf("aws", "aws.second"))
    doBasicCompletionTest(
      """
        module x {
          source = "./module/"
          providers = {
            "a<caret>" 
          }
        }
        """.trimIndent(), listOf("aws", "aws.second"))
    doBasicCompletionTest(
      """
        module x {
          source = "./module/"
          providers = {
            <caret>aws = "aws" 
          }
        }
        """.trimIndent(), listOf("aws", "aws.second"))
    doBasicCompletionTest(
      """
        module x {
          source = "./module/"
          providers = {
            "<caret>aws" = "aws" 
          }
        }
        """.trimIndent(), listOf("aws", "aws.second"))
    doBasicCompletionTest(
      """
        module x {
          source = "./module/"
          providers = {
            <caret> = "aws" 
          }
        }
        """.trimIndent(), listOf("aws", "aws.second"))
    doBasicCompletionTest(
      """
        module x {
          source = "./module/"
          providers = {
            "<caret>" = "aws" 
          }
        }
        """.trimIndent(), listOf("aws", "aws.second"))
    doBasicCompletionTest(
      """
        module x {
          source = "./module/"
          providers = {
            aws = "aws"
            <caret>
          }
        }
        """.trimIndent(), "aws.second")
    doBasicCompletionTest(
      """
        module x {
          source = "./module/"
          providers = {
            <caret>
            aws = "aws" 
          }
        }
        """.trimIndent(), "aws.second")


    doBasicCompletionTest(
      """
        module x {
          source = "./module/"
          providers = {
            aws = <caret> 
          }
        }
        """.trimIndent(), 0)
  }

  fun testModuleProvidersBlockCompletion() {
    myFixture.addFileToProject("module/a.tf", "provider aws {}\nprovider aws {alias=\"second\"}")
    doBasicCompletionTest(
      """
        module x {
          source = "./module/"
          providers {
            <caret> 
          }
        }
        """.trimIndent(), listOf("aws", "aws.second"))
    doBasicCompletionTest(
      """
        module x {
          source = "./module/"
          providers {
            "<caret>" 
          }
        }
        """.trimIndent(), listOf("aws", "aws.second"))
    doBasicCompletionTest(
      """
        module x {
          source = "./module/"
          providers {
            a<caret> 
          }
        }
        """.trimIndent(), listOf("aws", "aws.second"))
    doBasicCompletionTest(
      """
        module x {
          source = "./module/"
          providers {
            "a<caret>" 
          }
        }
        """.trimIndent(), listOf("aws", "aws.second"))
    doBasicCompletionTest(
      """
        module x {
          source = "./module/"
          providers {
            <caret>aws = "aws" 
          }
        }
        """.trimIndent(), listOf("aws", "aws.second"))
    doBasicCompletionTest(
      """
        module x {
          source = "./module/"
          providers {
            "<caret>aws" = "aws" 
          }
        }
        """.trimIndent(), listOf("aws", "aws.second"))
    doBasicCompletionTest(
      """
        module x {
          source = "./module/"
          providers {
            <caret> = "aws" 
          }
        }
        """.trimIndent(), listOf("aws", "aws.second"))
    doBasicCompletionTest(
      """
        module x {
          source = "./module/"
          providers {
            "<caret>" = "aws" 
          }
        }
        """.trimIndent(), listOf("aws", "aws.second"))
    doBasicCompletionTest(
      """
        module x {
          source = "./module/"
          providers {
            aws = "aws"
            <caret>
          }
        }
        """.trimIndent(), "aws.second")
    doBasicCompletionTest(
      """
        module x {
          source = "./module/"
          providers {
            <caret>
            aws = "aws" 
          }
        }
        """.trimIndent(), "aws.second")
    doBasicCompletionTest(
      """
        module x {
          source = "./module/"
          providers {
            aws = <caret> 
          }
        }
        """.trimIndent(), 0)
  }

  fun testModuleObjectVariableCompletion() {
    doBasicCompletionTest(
      """
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
      """.trimIndent(), listOf("var6", "var7", "var8"))

    myFixture.addFileToProject("directory/sub_directory/variables.tf", """
      variable "obj-var" {
        type = object({
          var1 = string
          var2 = list(string)
          var3 = object({
            var4 = string
            var5 = list(string)
          })
        })
      }
    """.trimIndent())
    doBasicCompletionTest(
      """
        module "test" {
          source = "./directory/sub_directory"

          obj-var = {
            <caret>
          }
        }
      """.trimIndent(), 3, "var1", "var2", "var3")
  }

  fun testModuleProvidersValueCompletion() {
    myFixture.addFileToProject("module/a.tf", "provider aws {}\nprovider aws {alias=\"second\"}")
    // via TfPropertyObjectKeyCompletionProvider
    doBasicCompletionTest(
      """
        provider aws {}module x {
          source = "./module/"
          providers = {
            aws=<caret> 
          }
        }
        """.trimIndent(), "aws")
    doBasicCompletionTest(
      """
        provider aws {alias="first"}module x {
          source = "./module/"
          providers {
            aws=<caret> 
          }
        }
        """.trimIndent(), "aws.first")
    doBasicCompletionTest(
      """
        provider aws {alias="first"}module x {
          source = "./module/"
          providers {
            aws="<caret>" 
          }
        }
        """.trimIndent(), "aws.first")
  }

  fun testModuleForEachCompletion() {
    doBasicCompletionTest("module 'x' { id = <caret>}", Matcher.not("each"))
    doBasicCompletionTest("module 'x' { for_each={}\n id = <caret>}", "each")
    doBasicCompletionTest("module 'x' { for_each={}\n id = each.<caret>}", 2, "key", "value")
  }

  fun testModuleDependsOnCompletion() {
    doBasicCompletionTest("resource x y {}\nmodule b {depends_on=['<caret>']}", 1, "x.y")
    doBasicCompletionTest("resource x y {}\nmodule b {depends_on=[\"<caret>\"]}", 1, "x.y")
    doBasicCompletionTest("data x y {}\nmodule b {depends_on=['<caret>']}", 1, "data.x.y")
    doBasicCompletionTest("data x y {}\nmodule b {depends_on=[\"<caret>\"]}", 1, "data.x.y")

    doBasicCompletionTest("resource x y {}\nmodule b {depends_on=[<caret>]}", 1, "x.y")
    doBasicCompletionTest("data x y {}\nmodule b {depends_on=[<caret>]}", 1, "data.x.y")

    doBasicCompletionTest("variable v{}\nresource x y {}\nmodule b {depends_on=[<caret>]}", 2, "x.y", "var.v")
    doBasicCompletionTest("variable v{}\ndata x y {}\nmodule b {depends_on=[<caret>]}", 2, "data.x.y", "var.v")
  }

  fun testImportBlockProperties() {
    doBasicCompletionTest("""
      import {
        <caret>
      }
    """.trimIndent(), "id", "to", "provider", "for_each")
  }

  fun testCompleteResourceFromAnotherModuleInImportBlock() {
    myFixture.addFileToProject("submodule/sub.tf", """
      resource "MyType" "MyName" {}
      
      """.trimIndent())
    myFixture.configureByText("main.tf", """
      import {
        id = "terraform"
        to = module.submodule.<caret>
      }
      
      module "submodule" {
        source = "./submodule"
      }
      
      """.trimIndent())
    myFixture.testCompletionVariants("main.tf", "MyType.MyName")
  }

  fun testCompleteResourceFromMovedBlock() {
    myFixture.addFileToProject("modules/compute/main.tf", """ 
      resource "aws_instance" "example1" { }
      resource "aws_instance" "example" { }
      """.trimIndent())
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
      
      """.trimIndent())
    myFixture.testCompletionVariants("main.tf", "aws_instance.example", "aws_instance.example1")
  }

  fun testOfficialResourcesVariants() {
    myFixture.configureByText("main.tf", """ 
      resource "aws_ec2_host<caret>"
      """.trimIndent())
    val lookupElements = myFixture.complete(CompletionType.BASIC, 1)
    assertEquals(2, lookupElements.size)

    val lookupStrings = lookupElements.map { element ->
      val resourceType = element.`object` as ResourceType
      val name = resourceType.type
      val provider = resourceType.provider.fullName
      "$name $provider"
    }.toList()

    assertEquals(listOf("aws_ec2_host hashicorp/aws", "awscc_ec2_host hashicorp/awscc"), lookupStrings)
  }

  fun testAllResourcesVariants() {
    myFixture.configureByText("main.tf", """ 
      resource "aws_ec2_host<caret>"
      """.trimIndent())
    val lookupElements = myFixture.complete(CompletionType.BASIC, 2)
    assertEquals(5, lookupElements.size)

    val lookupStrings = lookupElements.map { element ->
      val resourceType = element.`object` as ResourceType
      val name = resourceType.type
      val provider = resourceType.provider.fullName
      "$name $provider"
    }.toList()
    assertEquals(
      listOf("aws_ec2_host hashicorp/aws", "aws_ec2_host isometry/faws", "aws_ec2_host jandillenkofer/aws",
             "aws_ec2_host msalman899/aws", "awscc_ec2_host hashicorp/awscc"),
      lookupStrings
    )
  }

  fun testTerraformBlockCompletion() {
    myFixture.configureByText("main.tf", """
      terraform {
        <caret>
      }
    """.trimIndent())
    myFixture.testCompletionVariants("main.tf", "backend", "cloud", "experiments", "provider_meta", "required_providers",
                                     "required_version")
  }

  fun testTerraformBlockCompletionBackend() {
    myFixture.configureByText("main.tf", """
      terraform {
        backend "<caret>" {}
      }
    """.trimIndent())
    val model = globalModel
    val backends = model.backends.map { it.type }.toTypedArray()
    myFixture.testCompletionVariants("main.tf", *backends)
  }

  fun testAllEphemeralResourcesCompletion() {
    val matcher = getPartialMatcher(collectTypeNames<EphemeralType>())

    doBasicCompletionTest("ephemeral \"<caret>\"", matcher)
    doBasicCompletionTest("ephemeral \"<caret>\" \"test_name\"", matcher)
    doBasicCompletionTest("ephemeral \"<caret>\" \"test_name\" {}", matcher)
    doBasicCompletionTest("ephemeral \"<caret>\" \"test_name\"\n{}", matcher)
  }

  fun testEphemeralPropertiesCompletion() {
    doBasicCompletionTest(
      """
        ephemeral "test_ephemeral" "test_name" { <caret> }
        """.trimIndent(), 5, "depends_on", "count", "for_each", "provider", "lifecycle"
    )

    doBasicCompletionTest(
      "ephemeral \"azurerm_key_vault_certificate\" \"example\" { <caret> }",
      "name", "key_vault_id", "version"
    )
    doBasicCompletionTest(
      "ephemeral \"google_service_account_id_token\" \"test1\" { <caret> }",
      "target_audience", "target_service_account", "delegates", "include_email"
    )
    doBasicCompletionTest(
      "ephemeral \"aws_ssm_parameter\" \"test2\" { <caret> }",
      "arn", "with_decryption"
    )
  }

  fun testEphemeralExpressionCompletion() {
    doBasicCompletionTest(buildEphemeralTestText("ephemeral.<caret>"), 2, "aws_kms_secrets", "kubernetes_token_request_v1")
    doBasicCompletionTest(buildEphemeralTestText("ephemeral.aws_kms_secrets.<caret>"), 2, "test1", "test2")
    doBasicCompletionTest(buildEphemeralTestText("ephemeral.kubernetes_token_request_v1.<caret>"), 1, "test3")

    doBasicCompletionTest(buildEphemeralTestText("ephemeral.aws_kms_secrets.test2.<caret>"), "plaintext")
    doBasicCompletionTest(buildEphemeralTestText("ephemeral.kubernetes_token_request_v1.test3.<caret>"), "token")
  }

  private fun buildEphemeralTestText(expression: String): String = String.format(
    """
      ephemeral "aws_kms_secrets" "test1" {}
      ephemeral "aws_kms_secrets" "test2" {}
      ephemeral "kubernetes_token_request_v1" "test3" {}
        
      resource "aws_secretsmanager_secret_version" "db_password" {
        secret_id = %s
      }
    """.trimIndent(), expression
  )

  fun testRequiredProvidersCompletion() {
    val matcher = getPartialMatcher(collectBundledProviders())

    doBasicCompletionTest("terraform { required_providers { <caret> } }", matcher)
    doBasicCompletionTest(
      """
        terraform {
          required_providers {
            <caret>
          }
        }
      """.trimIndent(), "aws", "azurerm", "google", "kubernetes", "alicloud", "oci"
    )

    doBasicCompletionTest("""
      terraform {
        required_providers {
          aws = {
            <caret>
          }
        }
      }
    """.trimIndent(), 2, "source", "version")
  }

  fun testRequiredProvidersCompletionAfterProperty() {
    doBasicCompletionTest("""
      terraform {
        required_providers {
          aws = {
            source = "hashicorp/aws"
            <caret>
          }
        }
      }
    """.trimIndent(), 1, "version")

    doBasicCompletionTest("""
      terraform {
        required_providers {
          aws = {
            source = "hashicorp/aws"
            version = "~> 3.0"
            <caret>
          }
        }
      }
    """.trimIndent(), 0)
  }

  fun testSelfReferenceInPostCondition() {
    doBasicCompletionTest(
      """
      resource "aws_instance" "example" {
        instance_type = "t2.micro"
        ami           = "ami-abc123"

        lifecycle {
          postcondition {
            condition     = self.<caret> == "running"
            error_message = "EC2 instance must be running."
          }
        }
      }
    """.trimIndent(),
      "arn", "id", "instance_state", "password_data", "public_ip")
  }

  fun testNoCompletionsForEmptyDefaults() {
    doBasicCompletionTest("resource \"aws_instance\" \"test\" { ami = \"<caret>\" }", 0)
    doBasicCompletionTest("""
      data "aws_eks_addon" "instance" {
        addon_name   = ""
        cluster_name = "<caret>"
      }
    """.trimIndent(), 0)
  }

  companion object {
    fun collectBundledProviders(): List<String> = globalModel.allProviders().filter { it.tier in ProviderTier.PreferedProviders }
      .map { it.type }
      .sorted()
      .take(ENTRIES_LIST_SIZE)
      .toList()
  }
}

internal const val ENTRIES_LIST_SIZE = 900
internal const val COMPLETION_VARIANTS_LIMIT = (ENTRIES_LIST_SIZE + 100) * 2