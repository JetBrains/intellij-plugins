package com.intellij.openRewrite.run

import com.intellij.execution.actions.ConfigurationContext
import com.intellij.openRewrite.OpenRewriteLightHighlightingTestCase
import com.intellij.openRewrite.RECIPE_FILE_NAME
import com.intellij.testFramework.DumbModeTestUtils
import org.jetbrains.yaml.psi.YAMLFile
import org.jetbrains.yaml.psi.YAMLMapping
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertInstanceOf
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test

class OpenRewriteRunConfigurationProducerTest : OpenRewriteLightHighlightingTestCase() {
  @Test
  fun testYamlRecipe() = onEdt {
    val file = myFixture.configureByText(RECIPE_FILE_NAME, """
      type: specs.openrewrite.org/v1beta/recipe
      name: com.my.Recipe
    """.trimIndent()) as YAMLFile
    val key = (file.documents[0].topLevelValue as YAMLMapping).getKeyValueByKey("name")?.key
    assertNotNull(key)
    val configuration = ConfigurationContext(key!!).configuration
    assertNotNull(configuration)
    val rewriteConfiguration = assertInstanceOf(OpenRewriteRunConfiguration::class.java, configuration!!.configuration)
    assertEquals("com.my.Recipe", rewriteConfiguration.activeRecipes)
    assertEquals(project.basePath, rewriteConfiguration.workingDirectory)
  }

  @Test
  fun testYamlRecipeImDumbMode() = onEdt {
    val file = myFixture.configureByText(RECIPE_FILE_NAME, """
      type: specs.openrewrite.org/v1beta/recipe
      name: com.my.Recipe
    """.trimIndent()) as YAMLFile
    val key = (file.documents[0].topLevelValue as YAMLMapping).getKeyValueByKey("name")?.key
    assertNotNull(key)
    DumbModeTestUtils.runInDumbModeSynchronously(myFixture.getProject()) {
      val configuration = ConfigurationContext(key!!).configuration
      assertNotNull(configuration)
      assertInstanceOf(OpenRewriteRunConfiguration::class.java, configuration!!.configuration)
    }
  }

  @Test
  fun testYamlStyle() = onEdt {
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