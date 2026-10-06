package com.intellij.openRewrite.recipe

import com.intellij.openRewrite.OPTION_CLASS_NAME
import com.intellij.openRewrite.OpenRewriteLightHighlightingTestCase
import com.intellij.openRewrite.RECIPE_CLASS_NAME
import com.intellij.openRewrite.RECIPE_FILE_NAME
import com.intellij.openRewrite.STYLE_CLASS_NAME
import com.intellij.openapi.application.readAction
import com.intellij.testFramework.common.timeoutRunBlocking
import org.jetbrains.yaml.psi.YAMLFile
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertFalse
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertTrue
import org.junit.jupiter.api.Test

class OpenRewriteRecipeServiceTest : OpenRewriteLightHighlightingTestCase() {
  @Test
  fun testJavaRecipeDescriptor(): Unit = timeoutRunBlocking {
    val recipeClass = myFixture.addClass("""
      package com;

      public class MyRecipe extends ${RECIPE_CLASS_NAME} {
        @Override
        public String getDisplayName() {
          return "My Recipe";
        }

        @Override
        public String getDescription() {
          return "My test recipe";
        }
      }
    """.trimIndent())
    readAction {
      val descriptor = OpenRewriteRecipeService.getInstance(project).findDescriptor("com.MyRecipe", null, OpenRewriteType.RECIPE)
      assertNotNull(descriptor)
      assertEquals("com.MyRecipe", descriptor!!.name)
      assertEquals("My Recipe", descriptor.displayName)
      assertEquals("My test recipe", descriptor.description)
      assertFalse(descriptor.isComposite)
      assertEquals(recipeClass, descriptor.declaration.retrieve())
    }
  }

  @Test
  fun testJavaOptionDescriptor(): Unit = timeoutRunBlocking {
    val recipeClass = myFixture.addClass("""
      package com;

      public class MyRecipe extends ${RECIPE_CLASS_NAME} {
        @${OPTION_CLASS_NAME}(required = false)
        public String notRequired;

        @${OPTION_CLASS_NAME}(
            displayName = "My Option",
            description = "My option description",
            example = "value1",
            valid = {"value1", "value2" })
        public String option;
      }
    """.trimIndent())
    readAction {
      val descriptor = OpenRewriteRecipeService.getInstance(project).findDescriptor("com.MyRecipe", null, OpenRewriteType.RECIPE)
      assertNotNull(descriptor)

      val options = descriptor!!.options
      val notRequired = options.find { it.name == "notRequired" }
      assertNotNull(notRequired)
      assertFalse(notRequired!!.required)
      val option = options.find { it.name == "option" }
      assertNotNull(option)
      assertEquals("My Option", option!!.displayName)
      assertEquals("My option description", option.description)
      assertEquals("value1", option.example)
      assertEquals(listOf("value1", "value2"), option.valid)
      assertTrue(option.required)
      assertEquals("java.lang.String", option.typePointer.type!!.canonicalText)
      val optionField = recipeClass.fields.find { it.name == "option" }
      assertNotNull(optionField)
      assertEquals(optionField, option.declaration.retrieve())
    }
  }

  @Test
  fun testYamlRecipeDescriptor(): Unit = timeoutRunBlocking {
    val recipeFile = myFixture.addFileToProject(RECIPE_FILE_NAME, """
      type: specs.openrewrite.org/v1beta/recipe
      name: com.first
      displayName: My Recipe
      description: My test recipe
      recipeList:
        - com.second
        - com.third
      ---
      type: specs.openrewrite.org/v1beta/recipe
      name: com.second
      ---
      type: specs.openrewrite.org/v1beta/recipe
      name: com.third
    """.trimIndent()) as YAMLFile

    readAction {
      val descriptor = OpenRewriteRecipeService.getInstance(project).findDescriptor("com.first", recipeFile, OpenRewriteType.RECIPE)
      assertNotNull(descriptor)
      assertEquals("com.first", descriptor!!.name)
      assertEquals("My Recipe", descriptor.displayName)
      assertEquals("My test recipe", descriptor.description)
      assertTrue(descriptor.isComposite)
      assertEquals(recipeFile.documents[0], descriptor.declaration.retrieve())
    }
  }

  @Test
  fun testJavaStyleDescriptor(): Unit = timeoutRunBlocking {
    val styleClass = myFixture.addClass("""
      package com;

      public class MyStyle implements ${STYLE_CLASS_NAME} {
        public static final String NOT_AN_OPTION;
        public String option;
      }
    """.trimIndent())
    readAction {
      val descriptor = OpenRewriteRecipeService.getInstance(project).findDescriptor("com.MyStyle", null, OpenRewriteType.STYLE)
      assertNotNull(descriptor)

      assertEquals("com.MyStyle", descriptor!!.name)
      assertFalse(descriptor.isComposite)
      assertEquals(styleClass, descriptor.declaration.retrieve())

      assertEquals(1, descriptor.options.size)
      val option = descriptor.options.single()
      assertEquals("option", option.name)
      assertFalse(option.required)
      assertEquals("java.lang.String", option.typePointer.type!!.canonicalText)
      val optionField = styleClass.fields.find { it.name == "option" }
      assertNotNull(optionField)
      assertEquals(optionField, option.declaration.retrieve())
    }
  }

  @Test
  fun testYamlStyleDescriptor(): Unit = timeoutRunBlocking {
    val styleFile = myFixture.addFileToProject(RECIPE_FILE_NAME, """
      type: specs.openrewrite.org/v1beta/style
      name: com.first
      displayName: My Style
      description: My test style
      styleConfigs:
        - com.second
        - com.third
      ---
      type: specs.openrewrite.org/v1beta/style
      name: com.second
      ---
      type: specs.openrewrite.org/v1beta/style
      name: com.third
    """.trimIndent()) as YAMLFile

    readAction {
      val descriptor = OpenRewriteRecipeService.getInstance(project).findDescriptor("com.first", styleFile, OpenRewriteType.STYLE)
      assertNotNull(descriptor)
      assertEquals("com.first", descriptor!!.name)
      assertEquals("My Style", descriptor.displayName)
      assertEquals("My test style", descriptor.description)
      assertTrue(descriptor.isComposite)
      assertEquals(styleFile.documents[0], descriptor.declaration.retrieve())
    }
  }
}