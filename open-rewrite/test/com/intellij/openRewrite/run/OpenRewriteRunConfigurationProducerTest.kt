package com.intellij.openRewrite.run

import com.intellij.execution.actions.ConfigurationContext
import com.intellij.openRewrite.OpenRewriteLightHighlightingTestCase
import com.intellij.openRewrite.RECIPE_FILE_NAME
import com.intellij.testFramework.DumbModeTestUtils
import org.jetbrains.yaml.psi.YAMLFile
import org.jetbrains.yaml.psi.YAMLMapping

class OpenRewriteRunConfigurationProducerTest : OpenRewriteLightHighlightingTestCase() {
  fun testYamlRecipe() {
    val file = myFixture.configureByText(RECIPE_FILE_NAME, """
      type: specs.openrewrite.org/v1beta/recipe
      name: com.my.Recipe
    """.trimIndent()) as YAMLFile
    val key = (file.documents[0].topLevelValue as YAMLMapping).getKeyValueByKey("name")?.key
    assertNotNull(key)
    val configuration = ConfigurationContext(key!!).configuration
    assertNotNull(configuration)
    val rewriteConfiguration = assertInstanceOf(configuration!!.configuration, OpenRewriteRunConfiguration::class.java)
    assertEquals("com.my.Recipe", rewriteConfiguration.activeRecipes)
    assertEquals(project.basePath, rewriteConfiguration.workingDirectory)
  }

  fun testYamlRecipeImDumbMode() {
    val file = myFixture.configureByText(RECIPE_FILE_NAME, """
      type: specs.openrewrite.org/v1beta/recipe
      name: com.my.Recipe
    """.trimIndent()) as YAMLFile
    val key = (file.documents[0].topLevelValue as YAMLMapping).getKeyValueByKey("name")?.key
    assertNotNull(key)
    DumbModeTestUtils.runInDumbModeSynchronously(myFixture.getProject()) {
      val configuration = ConfigurationContext(key!!).configuration
      assertNotNull(configuration)
      assertInstanceOf(configuration!!.configuration, OpenRewriteRunConfiguration::class.java)
    }
  }

  fun testYamlStyle() {
    val file = myFixture.configureByText(RECIPE_FILE_NAME, """
      type: specs.openrewrite.org/v1beta/style
      name: com.my.Style
    """.trimIndent()) as YAMLFile
    val key = (file.documents[0].topLevelValue as YAMLMapping).getKeyValueByKey("name")?.key
    assertNotNull(key)
    val configuration = ConfigurationContext(key!!).configuration
    assertNull(configuration)
  }
}