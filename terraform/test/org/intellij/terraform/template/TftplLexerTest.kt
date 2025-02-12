package org.intellij.terraform.template

import com.intellij.lexer.Lexer
import com.intellij.testFramework.LexerTestCase
import org.intellij.terraform.template.lexer.TerraformTemplateLexer

internal class TftplLexerTest : LexerTestCase() {

  override fun createLexer(): Lexer = TerraformTemplateLexer()
  override fun getDirPath(): String = "unused"

  fun testBrokenFor() {
    doTest(
      """
      %{~ for a in b ~}

      %{~~~ endfor }

      """.trimIndent(),
      """
        TEMPLATE_START ('%{~')
        WHITE_SPACE (' ')
        for ('for')
        WHITE_SPACE (' ')
        ID ('a')
        WHITE_SPACE (' ')
        in ('in')
        WHITE_SPACE (' ')
        ID ('b')
        WHITE_SPACE (' ')
        } ('~}')
        DATA_LANGUAGE_TOKEN_UNPARSED ('\n\n')
        TEMPLATE_START ('%{~')
        ~ ('~')
        ~ ('~')
        WHITE_SPACE (' ')
        endfor ('endfor')
        WHITE_SPACE (' ')
        } ('}')
        DATA_LANGUAGE_TOKEN_UNPARSED ('\n') 
      """.trimIndent())
  }

  fun testInvalidCharactersBeforeForLoopPin() {
    doTest("""
      %{ %{ for a in }
      %{endfor}
    """.trimIndent(),
           """
             TEMPLATE_START ('%{')
             WHITE_SPACE (' ')
             % ('%')
             BAD_CHARACTER ('{')
             WHITE_SPACE (' ')
             for ('for')
             WHITE_SPACE (' ')
             ID ('a')
             WHITE_SPACE (' ')
             in ('in')
             WHITE_SPACE (' ')
             } ('}')
             DATA_LANGUAGE_TOKEN_UNPARSED ('\n')
             TEMPLATE_START ('%{')
             endfor ('endfor')
             } ('}')
           """.trimIndent())

  }

  fun testForLoop() {
    doTest("""
      %{ for a in }
        123
      %{endfor~}
    """.trimIndent(),
    """
      TEMPLATE_START ('%{')
      WHITE_SPACE (' ')
      for ('for')
      WHITE_SPACE (' ')
      ID ('a')
      WHITE_SPACE (' ')
      in ('in')
      WHITE_SPACE (' ')
      } ('}')
      DATA_LANGUAGE_TOKEN_UNPARSED ('\n  123\n')
      TEMPLATE_START ('%{')
      endfor ('endfor')
      } ('~}')
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

  fun testDollarTemplateSegment() {
    doTest("""
      %{ if variable > 3 }
      foo
      $dollar{variable}
      bar
      %{ endif }
    """.trimIndent(), """
      TEMPLATE_START ('%{')
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
      TEMPLATE_START ('%{')
      WHITE_SPACE (' ')
      endif ('endif')
      WHITE_SPACE (' ')
      } ('}')
    """.trimIndent())
  }

  fun testDetectProhibitedInterpolationInsideTemplateSegment() {
    doTest("%{ if \${a} }", """
      TEMPLATE_START ('%{')
      WHITE_SPACE (' ')
      if ('if')
      WHITE_SPACE (' ')
      BAD_CHARACTER ('$dollar')
      BAD_CHARACTER ('{')
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
      TEMPLATE_START ('%{')
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
      TEMPLATE_START ('%{')
      WHITE_SPACE (' ')
      endif ('endif')
      WHITE_SPACE (' ')
      } ('}')
      DATA_LANGUAGE_TOKEN_UNPARSED ('\n  ')
      TEMPLATE_START ('%{')
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
      TEMPLATE_START ('%{')
      WHITE_SPACE (' ')
      endif ('endif')
      WHITE_SPACE (' ')
      } ('}')
      DATA_LANGUAGE_TOKEN_UNPARSED ('\n}')""".trimIndent())
  }

  fun testSkeleton() {
    doTest("%{} abc", """
      TEMPLATE_START ('%{')
      } ('}')
      DATA_LANGUAGE_TOKEN_UNPARSED (' abc')
    """.trimIndent())
  }

  fun testIfCondition() {
    doTest("%{ if method(a, b) } { true } %{ else } { false }",
           """
             TEMPLATE_START ('%{')
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
             TEMPLATE_START ('%{')
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
        TEMPLATE_START ('%{')
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
            TEMPLATE_START ('%{')
            WHITE_SPACE (' ')
            if ('if')
            WHITE_SPACE (' ')
            BAD_CHARACTER ('{')
            true ('true')
            } ('}')
            DATA_LANGUAGE_TOKEN_UNPARSED (' }')
           """.trimIndent())
  }

  fun testIncorrectBracesInDataLanguage() {
    doTest("%{ if true } }}}} %{ else } }}{{{}",
           """
             TEMPLATE_START ('%{')
             WHITE_SPACE (' ')
             if ('if')
             WHITE_SPACE (' ')
             true ('true')
             WHITE_SPACE (' ')
             } ('}')
             DATA_LANGUAGE_TOKEN_UNPARSED (' }}}} ')
             TEMPLATE_START ('%{')
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
            TEMPLATE_START ('%{')
            WHITE_SPACE (' ')
            if ('if')
            WHITE_SPACE (' ')
            true ('true')
            WHITE_SPACE (' ')
            } ('}')
            DATA_LANGUAGE_TOKEN_UNPARSED (' { yes } ')
            TEMPLATE_START ('%{')
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
             TEMPLATE_START ('%{')
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
             TEMPLATE_START ('%{')
             WHITE_SPACE (' ')
             else ('else')
             WHITE_SPACE (' ')
             } ('}')
             DATA_LANGUAGE_TOKEN_UNPARSED (' intellij  2.2 ')
             TEMPLATE_START ('%{')
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
             TEMPLATE_START ('%{')
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
             TEMPLATE_START ('%{')
             WHITE_SPACE (' ')
             else ('else')
             WHITE_SPACE (' ')
             } ('}')
             DATA_LANGUAGE_TOKEN_UNPARSED (' 2.2 ')
             TEMPLATE_START ('%{')
             WHITE_SPACE (' ')
             endif ('endif')
             WHITE_SPACE (' ')
             } ('}')
           """.trimIndent()
    )
  }
}