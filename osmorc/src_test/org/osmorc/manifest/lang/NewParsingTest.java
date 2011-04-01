package org.osmorc.manifest.lang;

import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.DebugUtil;
import com.intellij.testFramework.LightIdeaTestCase;

public class NewParsingTest extends LightIdeaTestCase {
  public void testEmpty() {
    doTest("",
           "ManifestFile:MANIFEST.MF\n" +
           "  <empty list>\n");
  }

  public void testSpaces() {
    doTest("  ",
           "ManifestFile:MANIFEST.MF\n" +
           "  org.osmorc.manifest.lang.psi.impl.SectionImpl\n" +
           "    ManifestToken: ManifestTokenType: SIGNIFICANT_SPACE_TOKEN(' ')\n" +
           "    org.osmorc.manifest.lang.psi.impl.ClauseImpl\n" +
           "      org.osmorc.manifest.lang.psi.impl.HeaderValuePartImpl\n" +
           "        ManifestToken: ManifestTokenType: HEADER_VALUE_PART_TOKEN(' ')\n");
  }

  public void testNoHeader() {
    doTest(" some text",
           "ManifestFile:MANIFEST.MF\n" +
           "  org.osmorc.manifest.lang.psi.impl.SectionImpl\n" +
           "    ManifestToken: ManifestTokenType: SIGNIFICANT_SPACE_TOKEN(' ')\n" +
           "    org.osmorc.manifest.lang.psi.impl.ClauseImpl\n" +
           "      org.osmorc.manifest.lang.psi.impl.HeaderValuePartImpl\n" +
           "        ManifestToken: ManifestTokenType: HEADER_VALUE_PART_TOKEN('some text')\n");
  }

  public void testNewLine() {
    doTest("\n",
           "ManifestFile:MANIFEST.MF\n" +
           "  org.osmorc.manifest.lang.psi.impl.SectionImpl\n" +
           "    <empty list>\n" +
           "  ManifestToken: ManifestTokenType: SECTION_END_TOKEN('\\n')\n");
  }

  public void testNewLines() {
    doTest("\n\n",
           "ManifestFile:MANIFEST.MF\n" +
           "  org.osmorc.manifest.lang.psi.impl.SectionImpl\n" +
           "    <empty list>\n" +
           "  ManifestToken: ManifestTokenType: SECTION_END_TOKEN('\\n')\n" +
           "  org.osmorc.manifest.lang.psi.impl.SectionImpl\n" +
           "    <empty list>\n" +
           "  ManifestToken: ManifestTokenType: SECTION_END_TOKEN('\\n')\n");
  }

  public void testSimple() {
    doTest("Manifest-Version: 1.0\n",
           "ManifestFile:MANIFEST.MF\n" +
           "  org.osmorc.manifest.lang.psi.impl.SectionImpl\n" +
           "    org.osmorc.manifest.lang.psi.impl.HeaderImpl\n" +
           "      ManifestToken: ManifestTokenType: HEADER_NAME_TOKEN('Manifest-Version')\n" +
           "      ManifestToken: ManifestTokenType: COLON_TOKEN(':')\n" +
           "      ManifestToken: ManifestTokenType: SIGNIFICANT_SPACE_TOKEN(' ')\n" +
           "      org.osmorc.manifest.lang.psi.impl.ClauseImpl\n" +
           "        org.osmorc.manifest.lang.psi.impl.HeaderValuePartImpl\n" +
           "          ManifestToken: ManifestTokenType: HEADER_VALUE_PART_TOKEN('1.0')\n" +
           "          ManifestToken: ManifestTokenType: NEWLINE_TOKEN('\\n')\n");
  }

  public void testSimpleWithNewLines() throws Exception {
    doTest("Manifest-Version: 1.0\n\n",
           "ManifestFile:MANIFEST.MF\n" +
           "  org.osmorc.manifest.lang.psi.impl.SectionImpl\n" +
           "    org.osmorc.manifest.lang.psi.impl.HeaderImpl\n" +
           "      ManifestToken: ManifestTokenType: HEADER_NAME_TOKEN('Manifest-Version')\n" +
           "      ManifestToken: ManifestTokenType: COLON_TOKEN(':')\n" +
           "      ManifestToken: ManifestTokenType: SIGNIFICANT_SPACE_TOKEN(' ')\n" +
           "      org.osmorc.manifest.lang.psi.impl.ClauseImpl\n" +
           "        org.osmorc.manifest.lang.psi.impl.HeaderValuePartImpl\n" +
           "          ManifestToken: ManifestTokenType: HEADER_VALUE_PART_TOKEN('1.0')\n" +
           "          ManifestToken: ManifestTokenType: NEWLINE_TOKEN('\\n')\n" +
           "  ManifestToken: ManifestTokenType: SECTION_END_TOKEN('\\n')\n");
  }

  public void testSimpleIncomplete() throws Exception {
    doTest("Manifest-Version:",
           "ManifestFile:MANIFEST.MF\n" +
           "  org.osmorc.manifest.lang.psi.impl.SectionImpl\n" +
           "    org.osmorc.manifest.lang.psi.impl.HeaderImpl\n" +
           "      ManifestToken: ManifestTokenType: HEADER_NAME_TOKEN('Manifest-Version')\n" +
           "      ManifestToken: ManifestTokenType: COLON_TOKEN(':')\n");
  }

  public void testSimpleIncompleteWithNewLine() throws Exception {
    doTest("Manifest-Version:\n",
           "ManifestFile:MANIFEST.MF\n" +
           "  org.osmorc.manifest.lang.psi.impl.SectionImpl\n" +
           "    org.osmorc.manifest.lang.psi.impl.HeaderImpl\n" +
           "      ManifestToken: ManifestTokenType: HEADER_NAME_TOKEN('Manifest-Version')\n" +
           "      ManifestToken: ManifestTokenType: COLON_TOKEN(':')\n" +
           "      ManifestToken: ManifestTokenType: NEWLINE_TOKEN('\\n')\n");
  }

  public void testSimpleIncompleteWithNewLines() throws Exception {
    doTest("Manifest-Version:\n\n",
           "ManifestFile:MANIFEST.MF\n" +
           "  org.osmorc.manifest.lang.psi.impl.SectionImpl\n" +
           "    org.osmorc.manifest.lang.psi.impl.HeaderImpl\n" +
           "      ManifestToken: ManifestTokenType: HEADER_NAME_TOKEN('Manifest-Version')\n" +
           "      ManifestToken: ManifestTokenType: COLON_TOKEN(':')\n" +
           "      ManifestToken: ManifestTokenType: NEWLINE_TOKEN('\\n')\n" +
           "  ManifestToken: ManifestTokenType: SECTION_END_TOKEN('\\n')\n");
  }

  public void testSimpleWithContinuation() {
    doTest("Bundle-Vendor: Acme\n" +
           " Company\n",
           "ManifestFile:MANIFEST.MF\n" +
           "  org.osmorc.manifest.lang.psi.impl.SectionImpl\n" +
           "    org.osmorc.manifest.lang.psi.impl.HeaderImpl\n" +
           "      ManifestToken: ManifestTokenType: HEADER_NAME_TOKEN('Bundle-Vendor')\n" +
           "      ManifestToken: ManifestTokenType: COLON_TOKEN(':')\n" +
           "      ManifestToken: ManifestTokenType: SIGNIFICANT_SPACE_TOKEN(' ')\n" +
           "      org.osmorc.manifest.lang.psi.impl.ClauseImpl\n" +
           "        org.osmorc.manifest.lang.psi.impl.HeaderValuePartImpl\n" +
           "          ManifestToken: ManifestTokenType: HEADER_VALUE_PART_TOKEN('Acme')\n" +
           "          ManifestToken: ManifestTokenType: NEWLINE_TOKEN('\\n')\n" +
           "          ManifestToken: ManifestTokenType: SIGNIFICANT_SPACE_TOKEN(' ')\n" +
           "          ManifestToken: ManifestTokenType: HEADER_VALUE_PART_TOKEN('Company')\n" +
           "          ManifestToken: ManifestTokenType: NEWLINE_TOKEN('\\n')\n");
  }

  public void testTwoLines() {
    doTest("Manifest-Version: 1.0\n" +
           "Ant-Version: Apache Ant 1.6.5\n",
           "ManifestFile:MANIFEST.MF\n" +
           "  org.osmorc.manifest.lang.psi.impl.SectionImpl\n" +
           "    org.osmorc.manifest.lang.psi.impl.HeaderImpl\n" +
           "      ManifestToken: ManifestTokenType: HEADER_NAME_TOKEN('Manifest-Version')\n" +
           "      ManifestToken: ManifestTokenType: COLON_TOKEN(':')\n" +
           "      ManifestToken: ManifestTokenType: SIGNIFICANT_SPACE_TOKEN(' ')\n" +
           "      org.osmorc.manifest.lang.psi.impl.ClauseImpl\n" +
           "        org.osmorc.manifest.lang.psi.impl.HeaderValuePartImpl\n" +
           "          ManifestToken: ManifestTokenType: HEADER_VALUE_PART_TOKEN('1.0')\n" +
           "          ManifestToken: ManifestTokenType: NEWLINE_TOKEN('\\n')\n" +
           "    org.osmorc.manifest.lang.psi.impl.HeaderImpl\n" +
           "      ManifestToken: ManifestTokenType: HEADER_NAME_TOKEN('Ant-Version')\n" +
           "      ManifestToken: ManifestTokenType: COLON_TOKEN(':')\n" +
           "      ManifestToken: ManifestTokenType: SIGNIFICANT_SPACE_TOKEN(' ')\n" +
           "      org.osmorc.manifest.lang.psi.impl.ClauseImpl\n" +
           "        org.osmorc.manifest.lang.psi.impl.HeaderValuePartImpl\n" +
           "          ManifestToken: ManifestTokenType: HEADER_VALUE_PART_TOKEN('Apache Ant 1.6.5')\n" +
           "          ManifestToken: ManifestTokenType: NEWLINE_TOKEN('\\n')\n");
  }

  public void testTwoSections() {
    doTest("Manifest-Version: 1.0\n\n" +
           "Ant-Version: Apache Ant 1.6.5\n\n",
           "ManifestFile:MANIFEST.MF\n" +
           "  org.osmorc.manifest.lang.psi.impl.SectionImpl\n" +
           "    org.osmorc.manifest.lang.psi.impl.HeaderImpl\n" +
           "      ManifestToken: ManifestTokenType: HEADER_NAME_TOKEN('Manifest-Version')\n" +
           "      ManifestToken: ManifestTokenType: COLON_TOKEN(':')\n" +
           "      ManifestToken: ManifestTokenType: SIGNIFICANT_SPACE_TOKEN(' ')\n" +
           "      org.osmorc.manifest.lang.psi.impl.ClauseImpl\n" +
           "        org.osmorc.manifest.lang.psi.impl.HeaderValuePartImpl\n" +
           "          ManifestToken: ManifestTokenType: HEADER_VALUE_PART_TOKEN('1.0')\n" +
           "          ManifestToken: ManifestTokenType: NEWLINE_TOKEN('\\n')\n" +
           "  ManifestToken: ManifestTokenType: SECTION_END_TOKEN('\\n')\n" +
           "  org.osmorc.manifest.lang.psi.impl.SectionImpl\n" +
           "    org.osmorc.manifest.lang.psi.impl.HeaderImpl\n" +
           "      ManifestToken: ManifestTokenType: HEADER_NAME_TOKEN('Ant-Version')\n" +
           "      ManifestToken: ManifestTokenType: COLON_TOKEN(':')\n" +
           "      ManifestToken: ManifestTokenType: SIGNIFICANT_SPACE_TOKEN(' ')\n" +
           "      org.osmorc.manifest.lang.psi.impl.ClauseImpl\n" +
           "        org.osmorc.manifest.lang.psi.impl.HeaderValuePartImpl\n" +
           "          ManifestToken: ManifestTokenType: HEADER_VALUE_PART_TOKEN('Apache Ant 1.6.5')\n" +
           "          ManifestToken: ManifestTokenType: NEWLINE_TOKEN('\\n')\n" +
           "  ManifestToken: ManifestTokenType: SECTION_END_TOKEN('\\n')\n");
  }

  private static void doTest(final String source, final String expected) {
    final PsiFile file = createLightFile("MANIFEST.MF", source);
    assertEquals(expected,
                 DebugUtil.psiToString(file, true).replaceAll("@[0-9a-f]+", ""));
  }
}
