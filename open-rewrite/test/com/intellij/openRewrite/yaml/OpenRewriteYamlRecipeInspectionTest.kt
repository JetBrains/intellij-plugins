package com.intellij.openRewrite.yaml

import com.intellij.openRewrite.OPTION_CLASS_NAME
import com.intellij.openRewrite.OpenRewriteLightHighlightingTestCase
import com.intellij.openRewrite.RECIPE_CLASS_NAME
import com.intellij.openRewrite.RECIPE_FILE_NAME

class OpenRewriteYamlRecipeInspectionTest : OpenRewriteLightHighlightingTestCase() {
  fun testHighlighting() {
    addRecipes()
    myFixture.configureByText(RECIPE_FILE_NAME, """
      type: specs.openrewrite.org/v1beta/recipe
      name: com.first
      recipeList:
        - <error descr="Missing required option 'option'">com.MyRecipe</error>:
            boolOption: <error descr="Cannot convert 'not_true' to boolean">not_true</error>
            intOption: <error descr="Cannot convert 'double' to int">double</error>
            valueOption: <error descr="Invalid value 'three', must be one of one|two">three</error>
        - <error descr="Missing required option 'option'">com.MyOther</error>
        - com.MyThird
    """.trimIndent())
    myFixture.enableInspections(OpenRewriteYamlRecipeInspection())
    myFixture.allowTreeAccessForAllFiles()
    myFixture.testHighlighting(true, true, true)
  }

  private fun addRecipes() {
    myFixture.addClass("""
      package com;
      
      public class MyRecipe extends $RECIPE_CLASS_NAME {
        @$OPTION_CLASS_NAME
        public String option;
        @$OPTION_CLASS_NAME
        public boolean boolOption;
        @$OPTION_CLASS_NAME
        public int intOption;
        @$OPTION_CLASS_NAME(valid ={"one", "two"})
        public String valueOption;
      }
    """.trimIndent())
    myFixture.addClass("""
      package com;
      
      public class MyOther extends $RECIPE_CLASS_NAME {
        @$OPTION_CLASS_NAME
        public String option;  
      }
    """.trimIndent())
    myFixture.addClass("""
      package com;
      
      public class MyThird extends $RECIPE_CLASS_NAME {
        @$OPTION_CLASS_NAME
        public boolean option;  
      }
    """.trimIndent())
  }
}