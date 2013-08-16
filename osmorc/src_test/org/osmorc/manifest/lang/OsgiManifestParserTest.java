package org.osmorc.manifest.lang;

import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.DebugUtil;
import com.intellij.testFramework.LightIdeaTestCase;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class OsgiManifestParserTest extends LightIdeaTestCase {
  public void testEmptyHeader() {
    doTest("Import-Package: ",

           "ManifestFile:MANIFEST.MF\n" +
           "  Section\n" +
           "    Header:Import-Package\n" +
           "      ManifestToken:HEADER_NAME_TOKEN('Import-Package')\n" +
           "      ManifestToken:COLON_TOKEN(':')\n" +
           "      ManifestToken:SIGNIFICANT_SPACE_TOKEN(' ')\n");
  }

  public void testSimpleClause() {
    doTest("Import-Package: com.acme\n",

           "ManifestFile:MANIFEST.MF\n" +
           "  Section\n" +
           "    Header:Import-Package\n" +
           "      ManifestToken:HEADER_NAME_TOKEN('Import-Package')\n" +
           "      ManifestToken:COLON_TOKEN(':')\n" +
           "      ManifestToken:SIGNIFICANT_SPACE_TOKEN(' ')\n" +
           "      Clause\n" +
           "        HeaderValuePart\n" +
           "          ManifestToken:HEADER_VALUE_PART_TOKEN('com.acme')\n" +
           "          ManifestToken:NEWLINE_TOKEN('\\n')\n");
  }

  public void testEmptyClause() {
    doTest("Import-Package: ,com.acme\n",

           "ManifestFile:MANIFEST.MF\n" +
           "  Section\n" +
           "    Header:Import-Package\n" +
           "      ManifestToken:HEADER_NAME_TOKEN('Import-Package')\n" +
           "      ManifestToken:COLON_TOKEN(':')\n" +
           "      ManifestToken:SIGNIFICANT_SPACE_TOKEN(' ')\n" +
           "      Clause\n" +
           "        HeaderValuePart\n" +
           "          <empty list>\n" +
           "      ManifestToken:COMMA_TOKEN(',')\n" +
           "      Clause\n" +
           "        HeaderValuePart\n" +
           "          ManifestToken:HEADER_VALUE_PART_TOKEN('com.acme')\n" +
           "          ManifestToken:NEWLINE_TOKEN('\\n')\n");
  }

  public void testAttribute() {
    doTest("Import-Package: com.acme;version=\"1.0.0\"\n",

           "ManifestFile:MANIFEST.MF\n" +
           "  Section\n" +
           "    Header:Import-Package\n" +
           "      ManifestToken:HEADER_NAME_TOKEN('Import-Package')\n" +
           "      ManifestToken:COLON_TOKEN(':')\n" +
           "      ManifestToken:SIGNIFICANT_SPACE_TOKEN(' ')\n" +
           "      Clause\n" +
           "        HeaderValuePart\n" +
           "          ManifestToken:HEADER_VALUE_PART_TOKEN('com.acme')\n" +
           "        ManifestToken:SEMICOLON_TOKEN(';')\n" +
           "        Attribute:version\n" +
           "          HeaderValuePart\n" +
           "            ManifestToken:HEADER_VALUE_PART_TOKEN('version')\n" +
           "          ManifestToken:EQUALS_TOKEN('=')\n" +
           "          HeaderValuePart\n" +
           "            ManifestToken:QUOTE_TOKEN('\"')\n" +
           "            ManifestToken:HEADER_VALUE_PART_TOKEN('1.0.0')\n" +
           "            ManifestToken:QUOTE_TOKEN('\"')\n" +
           "            ManifestToken:NEWLINE_TOKEN('\\n')\n");
  }

  public void testAttributeWithoutName() {
    doTest("Import-Package: com.acme;=\"1.0.0\"\n",

           "ManifestFile:MANIFEST.MF\n" +
           "  Section\n" +
           "    Header:Import-Package\n" +
           "      ManifestToken:HEADER_NAME_TOKEN('Import-Package')\n" +
           "      ManifestToken:COLON_TOKEN(':')\n" +
           "      ManifestToken:SIGNIFICANT_SPACE_TOKEN(' ')\n" +
           "      Clause\n" +
           "        HeaderValuePart\n" +
           "          ManifestToken:HEADER_VALUE_PART_TOKEN('com.acme')\n" +
           "        ManifestToken:SEMICOLON_TOKEN(';')\n" +
           "        Attribute:\n" +
           "          HeaderValuePart\n" +
           "            <empty list>\n" +
           "          ManifestToken:EQUALS_TOKEN('=')\n" +
           "          HeaderValuePart\n" +
           "            ManifestToken:QUOTE_TOKEN('\"')\n" +
           "            ManifestToken:HEADER_VALUE_PART_TOKEN('1.0.0')\n" +
           "            ManifestToken:QUOTE_TOKEN('\"')\n" +
           "            ManifestToken:NEWLINE_TOKEN('\\n')\n");
  }

  public void testAttributeOutsideParameter() {
    doTest("Import-Package: ;version=\"1.0.0\"\n",

           "ManifestFile:MANIFEST.MF\n" +
           "  Section\n" +
           "    Header:Import-Package\n" +
           "      ManifestToken:HEADER_NAME_TOKEN('Import-Package')\n" +
           "      ManifestToken:COLON_TOKEN(':')\n" +
           "      ManifestToken:SIGNIFICANT_SPACE_TOKEN(' ')\n" +
           "      Clause\n" +
           "        HeaderValuePart\n" +
           "          <empty list>\n" +
           "        ManifestToken:SEMICOLON_TOKEN(';')\n" +
           "        Attribute:version\n" +
           "          HeaderValuePart\n" +
           "            ManifestToken:HEADER_VALUE_PART_TOKEN('version')\n" +
           "          ManifestToken:EQUALS_TOKEN('=')\n" +
           "          HeaderValuePart\n" +
           "            ManifestToken:QUOTE_TOKEN('\"')\n" +
           "            ManifestToken:HEADER_VALUE_PART_TOKEN('1.0.0')\n" +
           "            ManifestToken:QUOTE_TOKEN('\"')\n" +
           "            ManifestToken:NEWLINE_TOKEN('\\n')\n");
  }

  public void testAttributeWithoutNameOutsideParameter() {
    doTest("Import-Package: =\"1.0.0\"\n",

           "ManifestFile:MANIFEST.MF\n" +
           "  Section\n" +
           "    Header:Import-Package\n" +
           "      ManifestToken:HEADER_NAME_TOKEN('Import-Package')\n" +
           "      ManifestToken:COLON_TOKEN(':')\n" +
           "      ManifestToken:SIGNIFICANT_SPACE_TOKEN(' ')\n" +
           "      Clause\n" +
           "        Attribute:\n" +
           "          HeaderValuePart\n" +
           "            <empty list>\n" +
           "          ManifestToken:EQUALS_TOKEN('=')\n" +
           "          HeaderValuePart\n" +
           "            ManifestToken:QUOTE_TOKEN('\"')\n" +
           "            ManifestToken:HEADER_VALUE_PART_TOKEN('1.0.0')\n" +
           "            ManifestToken:QUOTE_TOKEN('\"')\n" +
           "            ManifestToken:NEWLINE_TOKEN('\\n')\n");
  }

  public void testDirective() {
    doTest("Import-Package: com.acme;resolution:=optional\n",

           "ManifestFile:MANIFEST.MF\n" +
           "  Section\n" +
           "    Header:Import-Package\n" +
           "      ManifestToken:HEADER_NAME_TOKEN('Import-Package')\n" +
           "      ManifestToken:COLON_TOKEN(':')\n" +
           "      ManifestToken:SIGNIFICANT_SPACE_TOKEN(' ')\n" +
           "      Clause\n" +
           "        HeaderValuePart\n" +
           "          ManifestToken:HEADER_VALUE_PART_TOKEN('com.acme')\n" +
           "        ManifestToken:SEMICOLON_TOKEN(';')\n" +
           "        Directive:resolution\n" +
           "          HeaderValuePart\n" +
           "            ManifestToken:HEADER_VALUE_PART_TOKEN('resolution')\n" +
           "          ManifestToken:COLON_TOKEN(':')\n" +
           "          ManifestToken:EQUALS_TOKEN('=')\n" +
           "          HeaderValuePart\n" +
           "            ManifestToken:HEADER_VALUE_PART_TOKEN('optional')\n" +
           "            ManifestToken:NEWLINE_TOKEN('\\n')\n");
  }

  public void testDirectiveWithoutName() {
    doTest("Import-Package: com.acme;:=optional\n",

           "ManifestFile:MANIFEST.MF\n" +
           "  Section\n" +
           "    Header:Import-Package\n" +
           "      ManifestToken:HEADER_NAME_TOKEN('Import-Package')\n" +
           "      ManifestToken:COLON_TOKEN(':')\n" +
           "      ManifestToken:SIGNIFICANT_SPACE_TOKEN(' ')\n" +
           "      Clause\n" +
           "        HeaderValuePart\n" +
           "          ManifestToken:HEADER_VALUE_PART_TOKEN('com.acme')\n" +
           "        ManifestToken:SEMICOLON_TOKEN(';')\n" +
           "        Directive:\n" +
           "          HeaderValuePart\n" +
           "            <empty list>\n" +
           "          ManifestToken:COLON_TOKEN(':')\n" +
           "          ManifestToken:EQUALS_TOKEN('=')\n" +
           "          HeaderValuePart\n" +
           "            ManifestToken:HEADER_VALUE_PART_TOKEN('optional')\n" +
           "            ManifestToken:NEWLINE_TOKEN('\\n')\n");
  }

  public void testDirectiveWithoutNameOutsideParameter() {
    doTest("Import-Package: :=optional\n",

           "ManifestFile:MANIFEST.MF\n" +
           "  Section\n" +
           "    Header:Import-Package\n" +
           "      ManifestToken:HEADER_NAME_TOKEN('Import-Package')\n" +
           "      ManifestToken:COLON_TOKEN(':')\n" +
           "      ManifestToken:SIGNIFICANT_SPACE_TOKEN(' ')\n" +
           "      Clause\n" +
           "        Directive:\n" +
           "          HeaderValuePart\n" +
           "            <empty list>\n" +
           "          ManifestToken:COLON_TOKEN(':')\n" +
           "          ManifestToken:EQUALS_TOKEN('=')\n" +
           "          HeaderValuePart\n" +
           "            ManifestToken:HEADER_VALUE_PART_TOKEN('optional')\n" +
           "            ManifestToken:NEWLINE_TOKEN('\\n')\n");
  }

  public void testDirectiveInvalidAssignmentTokens() {
    doTest("Import-Package: com.acme;resolution: =optional\n",

           "ManifestFile:MANIFEST.MF\n" +
           "  Section\n" +
           "    Header:Import-Package\n" +
           "      ManifestToken:HEADER_NAME_TOKEN('Import-Package')\n" +
           "      ManifestToken:COLON_TOKEN(':')\n" +
           "      ManifestToken:SIGNIFICANT_SPACE_TOKEN(' ')\n" +
           "      Clause\n" +
           "        HeaderValuePart\n" +
           "          ManifestToken:HEADER_VALUE_PART_TOKEN('com.acme')\n" +
           "        ManifestToken:SEMICOLON_TOKEN(';')\n" +
           "        Directive:resolution\n" +
           "          HeaderValuePart\n" +
           "            ManifestToken:HEADER_VALUE_PART_TOKEN('resolution')\n" +
           "          ManifestToken:COLON_TOKEN(':')\n" +
           "          HeaderValuePart\n" +
           "            ManifestToken:HEADER_VALUE_PART_TOKEN(' ')\n" +
           "            ManifestToken:EQUALS_TOKEN('=')\n" +
           "            ManifestToken:HEADER_VALUE_PART_TOKEN('optional')\n" +
           "            ManifestToken:NEWLINE_TOKEN('\\n')\n");
  }

  public void testSplitDirectiveAtColon() {
    doTest("Import-Package: com.acme;resolution:\n" +
           " =optional\n",

           "ManifestFile:MANIFEST.MF\n" +
           "  Section\n" +
           "    Header:Import-Package\n" +
           "      ManifestToken:HEADER_NAME_TOKEN('Import-Package')\n" +
           "      ManifestToken:COLON_TOKEN(':')\n" +
           "      ManifestToken:SIGNIFICANT_SPACE_TOKEN(' ')\n" +
           "      Clause\n" +
           "        HeaderValuePart\n" +
           "          ManifestToken:HEADER_VALUE_PART_TOKEN('com.acme')\n" +
           "        ManifestToken:SEMICOLON_TOKEN(';')\n" +
           "        Directive:resolution\n" +
           "          HeaderValuePart\n" +
           "            ManifestToken:HEADER_VALUE_PART_TOKEN('resolution')\n" +
           "          ManifestToken:COLON_TOKEN(':')\n" +
           "          ManifestToken:NEWLINE_TOKEN('\\n')\n" +
           "          ManifestToken:SIGNIFICANT_SPACE_TOKEN(' ')\n" +
           "          ManifestToken:EQUALS_TOKEN('=')\n" +
           "          HeaderValuePart\n" +
           "            ManifestToken:HEADER_VALUE_PART_TOKEN('optional')\n" +
           "            ManifestToken:NEWLINE_TOKEN('\\n')\n");
  }

  public void testAttributeAndDirective() {
    doTest("Import-Package: com.acme;version=\"1.0.0\";resolution:=optional\n",

           "ManifestFile:MANIFEST.MF\n" +
           "  Section\n" +
           "    Header:Import-Package\n" +
           "      ManifestToken:HEADER_NAME_TOKEN('Import-Package')\n" +
           "      ManifestToken:COLON_TOKEN(':')\n" +
           "      ManifestToken:SIGNIFICANT_SPACE_TOKEN(' ')\n" +
           "      Clause\n" +
           "        HeaderValuePart\n" +
           "          ManifestToken:HEADER_VALUE_PART_TOKEN('com.acme')\n" +
           "        ManifestToken:SEMICOLON_TOKEN(';')\n" +
           "        Attribute:version\n" +
           "          HeaderValuePart\n" +
           "            ManifestToken:HEADER_VALUE_PART_TOKEN('version')\n" +
           "          ManifestToken:EQUALS_TOKEN('=')\n" +
           "          HeaderValuePart\n" +
           "            ManifestToken:QUOTE_TOKEN('\"')\n" +
           "            ManifestToken:HEADER_VALUE_PART_TOKEN('1.0.0')\n" +
           "            ManifestToken:QUOTE_TOKEN('\"')\n" +
           "        ManifestToken:SEMICOLON_TOKEN(';')\n" +
           "        Directive:resolution\n" +
           "          HeaderValuePart\n" +
           "            ManifestToken:HEADER_VALUE_PART_TOKEN('resolution')\n" +
           "          ManifestToken:COLON_TOKEN(':')\n" +
           "          ManifestToken:EQUALS_TOKEN('=')\n" +
           "          HeaderValuePart\n" +
           "            ManifestToken:HEADER_VALUE_PART_TOKEN('optional')\n" +
           "            ManifestToken:NEWLINE_TOKEN('\\n')\n");
  }

  public void testAttributeAndDirectiveWithContinuationBeforeSemicolon() {
    doTest("Import-Package: com.acme;version=\"1.0.0\"\n" +
           " ;resolution:=optional\n",

           "ManifestFile:MANIFEST.MF\n" +
           "  Section\n" +
           "    Header:Import-Package\n" +
           "      ManifestToken:HEADER_NAME_TOKEN('Import-Package')\n" +
           "      ManifestToken:COLON_TOKEN(':')\n" +
           "      ManifestToken:SIGNIFICANT_SPACE_TOKEN(' ')\n" +
           "      Clause\n" +
           "        HeaderValuePart\n" +
           "          ManifestToken:HEADER_VALUE_PART_TOKEN('com.acme')\n" +
           "        ManifestToken:SEMICOLON_TOKEN(';')\n" +
           "        Attribute:version\n" +
           "          HeaderValuePart\n" +
           "            ManifestToken:HEADER_VALUE_PART_TOKEN('version')\n" +
           "          ManifestToken:EQUALS_TOKEN('=')\n" +
           "          HeaderValuePart\n" +
           "            ManifestToken:QUOTE_TOKEN('\"')\n" +
           "            ManifestToken:HEADER_VALUE_PART_TOKEN('1.0.0')\n" +
           "            ManifestToken:QUOTE_TOKEN('\"')\n" +
           "            ManifestToken:NEWLINE_TOKEN('\\n')\n" +
           "            ManifestToken:SIGNIFICANT_SPACE_TOKEN(' ')\n" +
           "        ManifestToken:SEMICOLON_TOKEN(';')\n" +
           "        Directive:resolution\n" +
           "          HeaderValuePart\n" +
           "            ManifestToken:HEADER_VALUE_PART_TOKEN('resolution')\n" +
           "          ManifestToken:COLON_TOKEN(':')\n" +
           "          ManifestToken:EQUALS_TOKEN('=')\n" +
           "          HeaderValuePart\n" +
           "            ManifestToken:HEADER_VALUE_PART_TOKEN('optional')\n" +
           "            ManifestToken:NEWLINE_TOKEN('\\n')\n");
  }

  public void testAttributeAndDirectiveWithContinuationAfterSemicolon() {
    doTest("Import-Package: com.acme;version=\"1.0.0\";\n" +
           " resolution:=optional\n",

           "ManifestFile:MANIFEST.MF\n" +
           "  Section\n" +
           "    Header:Import-Package\n" +
           "      ManifestToken:HEADER_NAME_TOKEN('Import-Package')\n" +
           "      ManifestToken:COLON_TOKEN(':')\n" +
           "      ManifestToken:SIGNIFICANT_SPACE_TOKEN(' ')\n" +
           "      Clause\n" +
           "        HeaderValuePart\n" +
           "          ManifestToken:HEADER_VALUE_PART_TOKEN('com.acme')\n" +
           "        ManifestToken:SEMICOLON_TOKEN(';')\n" +
           "        Attribute:version\n" +
           "          HeaderValuePart\n" +
           "            ManifestToken:HEADER_VALUE_PART_TOKEN('version')\n" +
           "          ManifestToken:EQUALS_TOKEN('=')\n" +
           "          HeaderValuePart\n" +
           "            ManifestToken:QUOTE_TOKEN('\"')\n" +
           "            ManifestToken:HEADER_VALUE_PART_TOKEN('1.0.0')\n" +
           "            ManifestToken:QUOTE_TOKEN('\"')\n" +
           "        ManifestToken:SEMICOLON_TOKEN(';')\n" +
           "        Directive:resolution\n" +
           "          HeaderValuePart\n" +
           "            ManifestToken:NEWLINE_TOKEN('\\n')\n" +
           "            ManifestToken:SIGNIFICANT_SPACE_TOKEN(' ')\n" +
           "            ManifestToken:HEADER_VALUE_PART_TOKEN('resolution')\n" +
           "          ManifestToken:COLON_TOKEN(':')\n" +
           "          ManifestToken:EQUALS_TOKEN('=')\n" +
           "          HeaderValuePart\n" +
           "            ManifestToken:HEADER_VALUE_PART_TOKEN('optional')\n" +
           "            ManifestToken:NEWLINE_TOKEN('\\n')\n");
  }

  public void testAttributeAndDirectiveWithBadContinuationAfterAttributeName() {
    doTest("Import-Package: com.acme;attr\n" +
           "=attr_value;dir:=dir_value\n",

           "ManifestFile:MANIFEST.MF\n" +
           "  Section\n" +
           "    Header:Import-Package\n" +
           "      ManifestToken:HEADER_NAME_TOKEN('Import-Package')\n" +
           "      ManifestToken:COLON_TOKEN(':')\n" +
           "      ManifestToken:SIGNIFICANT_SPACE_TOKEN(' ')\n" +
           "      Clause\n" +
           "        HeaderValuePart\n" +
           "          ManifestToken:HEADER_VALUE_PART_TOKEN('com.acme')\n" +
           "        ManifestToken:SEMICOLON_TOKEN(';')\n" +
           "        HeaderValuePart\n" +
           "          ManifestToken:HEADER_VALUE_PART_TOKEN('attr')\n" +
           "          ManifestToken:NEWLINE_TOKEN('\\n')\n" +
           "    PsiErrorElement:Header expected\n" +
           "      PsiElement(BAD_CHARACTER)('=')\n" +
           "      PsiElement(BAD_CHARACTER)('a')\n" +
           "      PsiElement(BAD_CHARACTER)('t')\n" +
           "      PsiElement(BAD_CHARACTER)('t')\n" +
           "      PsiElement(BAD_CHARACTER)('r')\n" +
           "      PsiElement(BAD_CHARACTER)('_')\n" +
           "      PsiElement(BAD_CHARACTER)('v')\n" +
           "      PsiElement(BAD_CHARACTER)('a')\n" +
           "      PsiElement(BAD_CHARACTER)('l')\n" +
           "      PsiElement(BAD_CHARACTER)('u')\n" +
           "      PsiElement(BAD_CHARACTER)('e')\n" +
           "      PsiElement(BAD_CHARACTER)(';')\n" +
           "      PsiElement(BAD_CHARACTER)('d')\n" +
           "      PsiElement(BAD_CHARACTER)('i')\n" +
           "      PsiElement(BAD_CHARACTER)('r')\n" +
           "      PsiElement(BAD_CHARACTER)(':')\n" +
           "      PsiElement(BAD_CHARACTER)('=')\n" +
           "      PsiElement(BAD_CHARACTER)('d')\n" +
           "      PsiElement(BAD_CHARACTER)('i')\n" +
           "      PsiElement(BAD_CHARACTER)('r')\n" +
           "      PsiElement(BAD_CHARACTER)('_')\n" +
           "      PsiElement(BAD_CHARACTER)('v')\n" +
           "      PsiElement(BAD_CHARACTER)('a')\n" +
           "      PsiElement(BAD_CHARACTER)('l')\n" +
           "      PsiElement(BAD_CHARACTER)('u')\n" +
           "      PsiElement(BAD_CHARACTER)('e')\n" +
           "      ManifestToken:NEWLINE_TOKEN('\\n')\n");
  }

  public void testTwoClauses() {
    doTest("Import-Package: com.acme.p;a=b,com.acme\n",

           "ManifestFile:MANIFEST.MF\n" +
           "  Section\n" +
           "    Header:Import-Package\n" +
           "      ManifestToken:HEADER_NAME_TOKEN('Import-Package')\n" +
           "      ManifestToken:COLON_TOKEN(':')\n" +
           "      ManifestToken:SIGNIFICANT_SPACE_TOKEN(' ')\n" +
           "      Clause\n" +
           "        HeaderValuePart\n" +
           "          ManifestToken:HEADER_VALUE_PART_TOKEN('com.acme.p')\n" +
           "        ManifestToken:SEMICOLON_TOKEN(';')\n" +
           "        Attribute:a\n" +
           "          HeaderValuePart\n" +
           "            ManifestToken:HEADER_VALUE_PART_TOKEN('a')\n" +
           "          ManifestToken:EQUALS_TOKEN('=')\n" +
           "          HeaderValuePart\n" +
           "            ManifestToken:HEADER_VALUE_PART_TOKEN('b')\n" +
           "      ManifestToken:COMMA_TOKEN(',')\n" +
           "      Clause\n" +
           "        HeaderValuePart\n" +
           "          ManifestToken:HEADER_VALUE_PART_TOKEN('com.acme')\n" +
           "          ManifestToken:NEWLINE_TOKEN('\\n')\n");
  }

  public void testVersionRange() {
    doTest("Import-Package: com.acme.p;version=\"(1.2.3,2.4.3]\",com.acme.l;version=\"[1.2.4,2.3.4)\"\n",

           "ManifestFile:MANIFEST.MF\n" +
           "  Section\n" +
           "    Header:Import-Package\n" +
           "      ManifestToken:HEADER_NAME_TOKEN('Import-Package')\n" +
           "      ManifestToken:COLON_TOKEN(':')\n" +
           "      ManifestToken:SIGNIFICANT_SPACE_TOKEN(' ')\n" +
           "      Clause\n" +
           "        HeaderValuePart\n" +
           "          ManifestToken:HEADER_VALUE_PART_TOKEN('com.acme.p')\n" +
           "        ManifestToken:SEMICOLON_TOKEN(';')\n" +
           "        Attribute:version\n" +
           "          HeaderValuePart\n" +
           "            ManifestToken:HEADER_VALUE_PART_TOKEN('version')\n" +
           "          ManifestToken:EQUALS_TOKEN('=')\n" +
           "          HeaderValuePart\n" +
           "            ManifestToken:QUOTE_TOKEN('\"')\n" +
           "            ManifestToken:OPENING_PARENTHESIS_TOKEN('(')\n" +
           "            ManifestToken:HEADER_VALUE_PART_TOKEN('1.2.3')\n" +
           "            ManifestToken:COMMA_TOKEN(',')\n" +
           "            ManifestToken:HEADER_VALUE_PART_TOKEN('2.4.3')\n" +
           "            ManifestToken:CLOSING_BRACKET_TOKEN(']')\n" +
           "            ManifestToken:QUOTE_TOKEN('\"')\n" +
           "      ManifestToken:COMMA_TOKEN(',')\n" +
           "      Clause\n" +
           "        HeaderValuePart\n" +
           "          ManifestToken:HEADER_VALUE_PART_TOKEN('com.acme.l')\n" +
           "        ManifestToken:SEMICOLON_TOKEN(';')\n" +
           "        Attribute:version\n" +
           "          HeaderValuePart\n" +
           "            ManifestToken:HEADER_VALUE_PART_TOKEN('version')\n" +
           "          ManifestToken:EQUALS_TOKEN('=')\n" +
           "          HeaderValuePart\n" +
           "            ManifestToken:QUOTE_TOKEN('\"')\n" +
           "            ManifestToken:OPENING_BRACKET_TOKEN('[')\n" +
           "            ManifestToken:HEADER_VALUE_PART_TOKEN('1.2.4')\n" +
           "            ManifestToken:COMMA_TOKEN(',')\n" +
           "            ManifestToken:HEADER_VALUE_PART_TOKEN('2.3.4')\n" +
           "            ManifestToken:CLOSING_PARENTHESIS_TOKEN(')')\n" +
           "            ManifestToken:QUOTE_TOKEN('\"')\n" +
           "            ManifestToken:NEWLINE_TOKEN('\\n')\n");
  }

  private static void doTest(String source, String expected) {
    PsiFile file = createLightFile("MANIFEST.MF", source);
    assertEquals(expected, DebugUtil.psiToString(file, true));
  }
}
