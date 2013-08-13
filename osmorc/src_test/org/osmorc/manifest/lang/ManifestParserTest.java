package org.osmorc.manifest.lang;

import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.DebugUtil;
import com.intellij.testFramework.LightIdeaTestCase;

/**
 * @author Robert F. Beeger (robert@beeger.net)
 */
public class ManifestParserTest extends LightIdeaTestCase {
  public void testEmpty() {
    doTest("",

           "ManifestFile:MANIFEST.MF\n" +
           "  <empty list>\n");
  }

  public void testSpaces() {
    doTest("  ",

           "ManifestFile:MANIFEST.MF\n" +
           "  Section\n" +
           "    ManifestToken:SIGNIFICANT_SPACE_TOKEN(' ')\n" +
           "    Clause\n" +
           "      HeaderValuePart\n" +
           "        ManifestToken:HEADER_VALUE_PART_TOKEN(' ')\n");
  }

  public void testNoHeader() {
    doTest(" some text",

           "ManifestFile:MANIFEST.MF\n" +
           "  Section\n" +
           "    ManifestToken:SIGNIFICANT_SPACE_TOKEN(' ')\n" +
           "    Clause\n" +
           "      HeaderValuePart\n" +
           "        ManifestToken:HEADER_VALUE_PART_TOKEN('some text')\n");
  }

  public void testNewLine() {
    doTest("\n",

           "ManifestFile:MANIFEST.MF\n" +
           "  Section\n" +
           "    <empty list>\n" +
           "  ManifestToken:SECTION_END_TOKEN('\\n')\n");
  }

  public void testNewLines() {
    doTest("\n\n",

           "ManifestFile:MANIFEST.MF\n" +
           "  Section\n" +
           "    <empty list>\n" +
           "  ManifestToken:SECTION_END_TOKEN('\\n')\n" +
           "  Section\n" +
           "    <empty list>\n" +
           "  ManifestToken:SECTION_END_TOKEN('\\n')\n");
  }

  public void testSimple() {
    doTest("Manifest-Version: 1.0\n",

           "ManifestFile:MANIFEST.MF\n" +
           "  Section\n" +
           "    Header:Manifest-Version\n" +
           "      ManifestToken:HEADER_NAME_TOKEN('Manifest-Version')\n" +
           "      ManifestToken:COLON_TOKEN(':')\n" +
           "      ManifestToken:SIGNIFICANT_SPACE_TOKEN(' ')\n" +
           "      Clause\n" +
           "        HeaderValuePart\n" +
           "          ManifestToken:HEADER_VALUE_PART_TOKEN('1.0')\n" +
           "          ManifestToken:NEWLINE_TOKEN('\\n')\n");
  }

  public void testSimpleWithSpaceInHeaderAssignment() {
    doTest("Manifest-Version : 1.0\n",

           "ManifestFile:MANIFEST.MF\n" +
           "  Section\n" +
           "    Header:Manifest-Version\n" +
           "      ManifestToken:HEADER_NAME_TOKEN('Manifest-Version')\n" +
           "      PsiElement(BAD_CHARACTER)(' ')\n" +
           "      ManifestToken:COLON_TOKEN(':')\n" +
           "      ManifestToken:SIGNIFICANT_SPACE_TOKEN(' ')\n" +
           "      Clause\n" +
           "        HeaderValuePart\n" +
           "          ManifestToken:HEADER_VALUE_PART_TOKEN('1.0')\n" +
           "          ManifestToken:NEWLINE_TOKEN('\\n')\n");
  }

  public void testSimpleMissingSpaceAfterHeaderAssignment1() {
    doTest("Manifest-Version:2\n",

           "ManifestFile:MANIFEST.MF\n" +
           "  Section\n" +
           "    Header:Manifest-Version\n" +
           "      ManifestToken:HEADER_NAME_TOKEN('Manifest-Version')\n" +
           "      ManifestToken:COLON_TOKEN(':')\n" +
           "      PsiElement(BAD_CHARACTER)('2')\n" +
           "      ManifestToken:NEWLINE_TOKEN('\\n')\n");
  }

  public void testSimpleMissingSpaceAfterHeaderAssignment2() {
    doTest("Specification-Vendor:name\n",

           "ManifestFile:MANIFEST.MF\n" +
           "  Section\n" +
           "    Header:Specification-Vendor\n" +
           "      ManifestToken:HEADER_NAME_TOKEN('Specification-Vendor')\n" +
           "      ManifestToken:COLON_TOKEN(':')\n" +
           "      PsiElement(BAD_CHARACTER)('n')\n" +
           "      Clause\n" +
           "        HeaderValuePart\n" +
           "          ManifestToken:HEADER_VALUE_PART_TOKEN('ame')\n" +
           "          ManifestToken:NEWLINE_TOKEN('\\n')\n");
  }

  public void testSimpleWithNewLines() {
    doTest("Manifest-Version: 1.0\n\n",

           "ManifestFile:MANIFEST.MF\n" +
           "  Section\n" +
           "    Header:Manifest-Version\n" +
           "      ManifestToken:HEADER_NAME_TOKEN('Manifest-Version')\n" +
           "      ManifestToken:COLON_TOKEN(':')\n" +
           "      ManifestToken:SIGNIFICANT_SPACE_TOKEN(' ')\n" +
           "      Clause\n" +
           "        HeaderValuePart\n" +
           "          ManifestToken:HEADER_VALUE_PART_TOKEN('1.0')\n" +
           "          ManifestToken:NEWLINE_TOKEN('\\n')\n" +
           "  ManifestToken:SECTION_END_TOKEN('\\n')\n");
  }

  public void testSimpleIncomplete() {
    doTest("Manifest-Version:",

           "ManifestFile:MANIFEST.MF\n" +
           "  Section\n" +
           "    Header:Manifest-Version\n" +
           "      ManifestToken:HEADER_NAME_TOKEN('Manifest-Version')\n" +
           "      ManifestToken:COLON_TOKEN(':')\n");
  }

  public void testSimpleIncompleteWithNewLine() {
    doTest("Manifest-Version:\n",

           "ManifestFile:MANIFEST.MF\n" +
           "  Section\n" +
           "    Header:Manifest-Version\n" +
           "      ManifestToken:HEADER_NAME_TOKEN('Manifest-Version')\n" +
           "      ManifestToken:COLON_TOKEN(':')\n" +
           "      ManifestToken:NEWLINE_TOKEN('\\n')\n");
  }

  public void testSimpleIncompleteWithNewLines() {
    doTest("Manifest-Version:\n\n",

           "ManifestFile:MANIFEST.MF\n" +
           "  Section\n" +
           "    Header:Manifest-Version\n" +
           "      ManifestToken:HEADER_NAME_TOKEN('Manifest-Version')\n" +
           "      ManifestToken:COLON_TOKEN(':')\n" +
           "      ManifestToken:NEWLINE_TOKEN('\\n')\n" +
           "  ManifestToken:SECTION_END_TOKEN('\\n')\n");
  }

  public void testSimpleWithContinuation() {
    doTest("Specification-Vendor: Acme\n" +
           " Company\n",

           "ManifestFile:MANIFEST.MF\n" +
           "  Section\n" +
           "    Header:Specification-Vendor\n" +
           "      ManifestToken:HEADER_NAME_TOKEN('Specification-Vendor')\n" +
           "      ManifestToken:COLON_TOKEN(':')\n" +
           "      ManifestToken:SIGNIFICANT_SPACE_TOKEN(' ')\n" +
           "      Clause\n" +
           "        HeaderValuePart\n" +
           "          ManifestToken:HEADER_VALUE_PART_TOKEN('Acme')\n" +
           "          ManifestToken:NEWLINE_TOKEN('\\n')\n" +
           "          ManifestToken:SIGNIFICANT_SPACE_TOKEN(' ')\n" +
           "          ManifestToken:HEADER_VALUE_PART_TOKEN('Company')\n" +
           "          ManifestToken:NEWLINE_TOKEN('\\n')\n");
  }

  public void testSimpleWithQuotedValue() {
    doTest("Implementation-Vendor: \"Apache Software Foundation\"\n",

           "ManifestFile:MANIFEST.MF\n" +
           "  Section\n" +
           "    Header:Implementation-Vendor\n" +
           "      ManifestToken:HEADER_NAME_TOKEN('Implementation-Vendor')\n" +
           "      ManifestToken:COLON_TOKEN(':')\n" +
           "      ManifestToken:SIGNIFICANT_SPACE_TOKEN(' ')\n" +
           "      Clause\n" +
           "        HeaderValuePart\n" +
           "          ManifestToken:QUOTE_TOKEN('\"')\n" +
           "          ManifestToken:HEADER_VALUE_PART_TOKEN('Apache Software Foundation')\n" +
           "          ManifestToken:QUOTE_TOKEN('\"')\n" +
           "          ManifestToken:NEWLINE_TOKEN('\\n')\n");
  }

  public void testSimpleHeaderValueStartsWithColon() {
    doTest("Implementation-Vendor: :value\n",

           "ManifestFile:MANIFEST.MF\n" +
           "  Section\n" +
           "    Header:Implementation-Vendor\n" +
           "      ManifestToken:HEADER_NAME_TOKEN('Implementation-Vendor')\n" +
           "      ManifestToken:COLON_TOKEN(':')\n" +
           "      ManifestToken:SIGNIFICANT_SPACE_TOKEN(' ')\n" +
           "      Clause\n" +
           "        HeaderValuePart\n" +
           "          ManifestToken:COLON_TOKEN(':')\n" +
           "          ManifestToken:HEADER_VALUE_PART_TOKEN('value')\n" +
           "          ManifestToken:NEWLINE_TOKEN('\\n')\n");
  }

  public void testSimpleHeaderValueStartsWithEquals() {
    doTest("Implementation-Vendor: =value\n",

           "ManifestFile:MANIFEST.MF\n" +
           "  Section\n" +
           "    Header:Implementation-Vendor\n" +
           "      ManifestToken:HEADER_NAME_TOKEN('Implementation-Vendor')\n" +
           "      ManifestToken:COLON_TOKEN(':')\n" +
           "      ManifestToken:SIGNIFICANT_SPACE_TOKEN(' ')\n" +
           "      Clause\n" +
           "        HeaderValuePart\n" +
           "          ManifestToken:EQUALS_TOKEN('=')\n" +
           "          ManifestToken:HEADER_VALUE_PART_TOKEN('value')\n" +
           "          ManifestToken:NEWLINE_TOKEN('\\n')\n");
  }

  public void testSimpleHeaderValueStartsWithSemicolon() {
    doTest("Implementation-Vendor: ;value\n",

           "ManifestFile:MANIFEST.MF\n" +
           "  Section\n" +
           "    Header:Implementation-Vendor\n" +
           "      ManifestToken:HEADER_NAME_TOKEN('Implementation-Vendor')\n" +
           "      ManifestToken:COLON_TOKEN(':')\n" +
           "      ManifestToken:SIGNIFICANT_SPACE_TOKEN(' ')\n" +
           "      Clause\n" +
           "        HeaderValuePart\n" +
           "          ManifestToken:SEMICOLON_TOKEN(';')\n" +
           "          ManifestToken:HEADER_VALUE_PART_TOKEN('value')\n" +
           "          ManifestToken:NEWLINE_TOKEN('\\n')\n");
  }

  public void testTwoHeaders() {
    doTest("Manifest-Version: 1.0\n" +
           "Ant-Version: Apache Ant 1.6.5\n",

           "ManifestFile:MANIFEST.MF\n" +
           "  Section\n" +
           "    Header:Manifest-Version\n" +
           "      ManifestToken:HEADER_NAME_TOKEN('Manifest-Version')\n" +
           "      ManifestToken:COLON_TOKEN(':')\n" +
           "      ManifestToken:SIGNIFICANT_SPACE_TOKEN(' ')\n" +
           "      Clause\n" +
           "        HeaderValuePart\n" +
           "          ManifestToken:HEADER_VALUE_PART_TOKEN('1.0')\n" +
           "          ManifestToken:NEWLINE_TOKEN('\\n')\n" +
           "    Header:Ant-Version\n" +
           "      ManifestToken:HEADER_NAME_TOKEN('Ant-Version')\n" +
           "      ManifestToken:COLON_TOKEN(':')\n" +
           "      ManifestToken:SIGNIFICANT_SPACE_TOKEN(' ')\n" +
           "      Clause\n" +
           "        HeaderValuePart\n" +
           "          ManifestToken:HEADER_VALUE_PART_TOKEN('Apache Ant 1.6.5')\n" +
           "          ManifestToken:NEWLINE_TOKEN('\\n')\n");
  }

  public void testTwoSections() {
    doTest("Manifest-Version: 1.0\n" +
           "\n" +
           "Ant-Version: Apache Ant 1.6.5\n\n",

           "ManifestFile:MANIFEST.MF\n" +
           "  Section\n" +
           "    Header:Manifest-Version\n" +
           "      ManifestToken:HEADER_NAME_TOKEN('Manifest-Version')\n" +
           "      ManifestToken:COLON_TOKEN(':')\n" +
           "      ManifestToken:SIGNIFICANT_SPACE_TOKEN(' ')\n" +
           "      Clause\n" +
           "        HeaderValuePart\n" +
           "          ManifestToken:HEADER_VALUE_PART_TOKEN('1.0')\n" +
           "          ManifestToken:NEWLINE_TOKEN('\\n')\n" +
           "  ManifestToken:SECTION_END_TOKEN('\\n')\n" +
           "  Section\n" +
           "    Header:Ant-Version\n" +
           "      ManifestToken:HEADER_NAME_TOKEN('Ant-Version')\n" +
           "      ManifestToken:COLON_TOKEN(':')\n" +
           "      ManifestToken:SIGNIFICANT_SPACE_TOKEN(' ')\n" +
           "      Clause\n" +
           "        HeaderValuePart\n" +
           "          ManifestToken:HEADER_VALUE_PART_TOKEN('Apache Ant 1.6.5')\n" +
           "          ManifestToken:NEWLINE_TOKEN('\\n')\n" +
           "  ManifestToken:SECTION_END_TOKEN('\\n')\n");
  }

  private static void doTest(String source, String expected) {
    PsiFile file = createLightFile("MANIFEST.MF", source);
    assertEquals(expected, DebugUtil.psiToString(file, true));
  }
}
