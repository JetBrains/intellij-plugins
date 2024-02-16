package org.intellij.terraform.template

import com.intellij.lexer.Lexer
import com.intellij.testFramework.LexerTestCase
import org.intellij.terraform.template.lexer.TerraformTemplateLexer

internal class TerraformTemplateLexerTest : LexerTestCase() {

  override fun createLexer(): Lexer = TerraformTemplateLexer()
  override fun getDirPath(): String = "unused"

  fun testForLoop() {
    doTest("""
      %{ for a in }
      
      %{endfor}
    """.trimIndent(),
    """
      %{ ('%{')
      WHITE_SPACE (' ')
      for ('for')
      WHITE_SPACE (' ')
      ID ('a')
      WHITE_SPACE (' ')
      in ('in')
      WHITE_SPACE (' ')
      } ('}')
      DATA_LANGUAGE_TOKEN_UNPARSED ('\n\n')
      %{ ('%{')
      endfor ('endfor')
      } ('}')
    """.trimIndent())
  }

  fun testString() {
    doTest(
      """
        "$dollar{hi}"
      """.trimIndent(),
      """
        DATA_LANGUAGE_TOKEN_UNPARSED ('"$dollar{hi}"')
      """.trimIndent())
  }

  fun testInvalidCode() {
    doTest("""
      $dollar{
        jsonencode({
          %{if cradle_v != "" }
          "cradle": $dollar{cradle_v},
          %{ endif }
        })
      }

    """.trimIndent(),
           """
      $dollar{ ('$dollar{')
      WHITE_SPACE ('\n  ')
      ID ('jsonencode')
      ( ('(')
      { ('{')
      WHITE_SPACE ('\n    ')
      % ('%')
      { ('{')
      if ('if')
      WHITE_SPACE (' ')
      ID ('cradle_v')
      WHITE_SPACE (' ')
      != ('!=')
      WHITE_SPACE (' ')
      DOUBLE_QUOTED_STRING ('""')
      WHITE_SPACE (' ')
      } ('}')
      DATA_LANGUAGE_TOKEN_UNPARSED ('\n    "cradle": ')
      $dollar{ ('$dollar{')
      ID ('cradle_v')
      } ('}')
      DATA_LANGUAGE_TOKEN_UNPARSED (',\n    ')
      %{ ('%{')
      WHITE_SPACE (' ')
      endif ('endif')
      WHITE_SPACE (' ')
      } ('}')
      DATA_LANGUAGE_TOKEN_UNPARSED ('\n  })\n}\n')
    """.trimIndent())
  }

  fun testDollarTemplateSegment() {
    doTest("""
      %{ if variable > 3 }
      foo
      $dollar{variable}
      bar
      %{ endif }
    """.trimIndent(), """
      %{ ('%{')
      WHITE_SPACE (' ')
      if ('if')
      WHITE_SPACE (' ')
      ID ('variable')
      WHITE_SPACE (' ')
      > ('>')
      WHITE_SPACE (' ')
      NUMBER ('3')
      WHITE_SPACE (' ')
      } ('}')
      DATA_LANGUAGE_TOKEN_UNPARSED ('\nfoo\n')
      $dollar{ ('$dollar{')
      ID ('variable')
      } ('}')
      DATA_LANGUAGE_TOKEN_UNPARSED ('\nbar\n')
      %{ ('%{')
      WHITE_SPACE (' ')
      endif ('endif')
      WHITE_SPACE (' ')
      } ('}')
    """.trimIndent())
  }

  fun testDetectProhibitedInterpolationInsideTemplateSegment() {
    doTest("%{ if \${a} }", """
      %{ ('%{')
      WHITE_SPACE (' ')
      if ('if')
      WHITE_SPACE (' ')
      $dollar ('$dollar')
      { ('{')
      ID ('a')
      } ('}')
      DATA_LANGUAGE_TOKEN_UNPARSED (' }')
    """.trimIndent())
  }

  fun testRealLife() {
    doTest("""
      {
        %{ if a > 4 ||  b == true && method(a, d) }
        "example_property": 123,
        %{ endif }
        %{if jade_v != "" }
        "jade": $dollar{jade_v},
        %{ endif }
      }
    """.trimIndent(),
           """
      DATA_LANGUAGE_TOKEN_UNPARSED ('{\n  ')
      %{ ('%{')
      WHITE_SPACE (' ')
      if ('if')
      WHITE_SPACE (' ')
      ID ('a')
      WHITE_SPACE (' ')
      > ('>')
      WHITE_SPACE (' ')
      NUMBER ('4')
      WHITE_SPACE (' ')
      || ('||')
      WHITE_SPACE ('  ')
      ID ('b')
      WHITE_SPACE (' ')
      == ('==')
      WHITE_SPACE (' ')
      true ('true')
      WHITE_SPACE (' ')
      && ('&&')
      WHITE_SPACE (' ')
      ID ('method')
      ( ('(')
      ID ('a')
      , (',')
      WHITE_SPACE (' ')
      ID ('d')
      ) (')')
      WHITE_SPACE (' ')
      } ('}')
      DATA_LANGUAGE_TOKEN_UNPARSED ('\n  "example_property": 123,\n  ')
      %{ ('%{')
      WHITE_SPACE (' ')
      endif ('endif')
      WHITE_SPACE (' ')
      } ('}')
      DATA_LANGUAGE_TOKEN_UNPARSED ('\n  ')
      %{ ('%{')
      if ('if')
      WHITE_SPACE (' ')
      ID ('jade_v')
      WHITE_SPACE (' ')
      != ('!=')
      WHITE_SPACE (' ')
      DOUBLE_QUOTED_STRING ('""')
      WHITE_SPACE (' ')
      } ('}')
      DATA_LANGUAGE_TOKEN_UNPARSED ('\n  "jade": ')
      $dollar{ ('$dollar{')
      ID ('jade_v')
      } ('}')
      DATA_LANGUAGE_TOKEN_UNPARSED (',\n  ')
      %{ ('%{')
      WHITE_SPACE (' ')
      endif ('endif')
      WHITE_SPACE (' ')
      } ('}')
      DATA_LANGUAGE_TOKEN_UNPARSED ('\n}')""".trimIndent())
  }

  fun testSkeleton() {
    doTest("%{} abc", """
      %{ ('%{')
      } ('}')
      DATA_LANGUAGE_TOKEN_UNPARSED (' abc')
    """.trimIndent())
  }

  fun testIfCondition() {
    doTest("%{ if method(a, b) } { true } %{ else } { false }",
           """
             %{ ('%{')
             WHITE_SPACE (' ')
             if ('if')
             WHITE_SPACE (' ')
             ID ('method')
             ( ('(')
             ID ('a')
             , (',')
             WHITE_SPACE (' ')
             ID ('b')
             ) (')')
             WHITE_SPACE (' ')
             } ('}')
             DATA_LANGUAGE_TOKEN_UNPARSED (' { true } ')
             %{ ('%{')
             WHITE_SPACE (' ')
             else ('else')
             WHITE_SPACE (' ')
             } ('}')
             DATA_LANGUAGE_TOKEN_UNPARSED (' { false }')
           """.trimIndent())
  }

  fun testJsonTemplate() {
    doTest(
      """
        %{if variable != "" }
        
      """.trimIndent(),
      """
        %{ ('%{')
        if ('if')
        WHITE_SPACE (' ')
        ID ('variable')
        WHITE_SPACE (' ')
        != ('!=')
        WHITE_SPACE (' ')
        DOUBLE_QUOTED_STRING ('""')
        WHITE_SPACE (' ')
        } ('}')
        DATA_LANGUAGE_TOKEN_UNPARSED ('\n')
      """.trimIndent()
    )
  }

  fun testOpeningBraceInIlSegment() {
    doTest("%{ if {true} }",
           """
            %{ ('%{')
            WHITE_SPACE (' ')
            if ('if')
            WHITE_SPACE (' ')
            { ('{')
            true ('true')
            } ('}')
            DATA_LANGUAGE_TOKEN_UNPARSED (' }')
           """.trimIndent())
  }

  fun testIncorrectBracesInDataLanguage() {
    doTest("%{ if true } }}}} %{ else } }}{{{}",
           """
             %{ ('%{')
             WHITE_SPACE (' ')
             if ('if')
             WHITE_SPACE (' ')
             true ('true')
             WHITE_SPACE (' ')
             } ('}')
             DATA_LANGUAGE_TOKEN_UNPARSED (' }}}} ')
             %{ ('%{')
             WHITE_SPACE (' ')
             else ('else')
             WHITE_SPACE (' ')
             } ('}')
             DATA_LANGUAGE_TOKEN_UNPARSED (' }}{{{}')
           """.trimIndent())
  }

  fun testCorrectBracesInDataLanguage() {
    doTest("%{ if true } { yes } %{ else } { no }",
           """
            %{ ('%{')
            WHITE_SPACE (' ')
            if ('if')
            WHITE_SPACE (' ')
            true ('true')
            WHITE_SPACE (' ')
            } ('}')
            DATA_LANGUAGE_TOKEN_UNPARSED (' { yes } ')
            %{ ('%{')
            WHITE_SPACE (' ')
            else ('else')
            WHITE_SPACE (' ')
            } ('}')
            DATA_LANGUAGE_TOKEN_UNPARSED (' { no }')
           """.trimIndent())
  }

  fun testExampleWithDataLanguage() {
    doTest("hello %{ if a.b.c } 1.3 world %{ else } intellij  2.2 %{ endif } rulezzz",
           """
             DATA_LANGUAGE_TOKEN_UNPARSED ('hello ')
             %{ ('%{')
             WHITE_SPACE (' ')
             if ('if')
             WHITE_SPACE (' ')
             ID ('a')
             . ('.')
             ID ('b')
             . ('.')
             ID ('c')
             WHITE_SPACE (' ')
             } ('}')
             DATA_LANGUAGE_TOKEN_UNPARSED (' 1.3 world ')
             %{ ('%{')
             WHITE_SPACE (' ')
             else ('else')
             WHITE_SPACE (' ')
             } ('}')
             DATA_LANGUAGE_TOKEN_UNPARSED (' intellij  2.2 ')
             %{ ('%{')
             WHITE_SPACE (' ')
             endif ('endif')
             WHITE_SPACE (' ')
             } ('}')
             DATA_LANGUAGE_TOKEN_UNPARSED (' rulezzz')
           """.trimIndent()
    )
  }

  fun testSimpleExamples() {
    doTest("%{ if a.b.c } 1.3 %{ else } 2.2 %{ endif }",
           """
             %{ ('%{')
             WHITE_SPACE (' ')
             if ('if')
             WHITE_SPACE (' ')
             ID ('a')
             . ('.')
             ID ('b')
             . ('.')
             ID ('c')
             WHITE_SPACE (' ')
             } ('}')
             DATA_LANGUAGE_TOKEN_UNPARSED (' 1.3 ')
             %{ ('%{')
             WHITE_SPACE (' ')
             else ('else')
             WHITE_SPACE (' ')
             } ('}')
             DATA_LANGUAGE_TOKEN_UNPARSED (' 2.2 ')
             %{ ('%{')
             WHITE_SPACE (' ')
             endif ('endif')
             WHITE_SPACE (' ')
             } ('}')
           """.trimIndent()
    )
  }
}