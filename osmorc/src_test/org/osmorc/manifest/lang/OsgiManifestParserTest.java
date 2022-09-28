/*
 * Copyright 2000-2016 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.osmorc.manifest.lang;

import com.intellij.psi.PsiFile;
import com.intellij.psi.impl.DebugUtil;
import com.intellij.testFramework.LightIdeaTestCase;

/**
 * @author <a href="mailto:robert@beeger.net">Robert F. Beeger</a>
 */
public class OsgiManifestParserTest extends LightIdeaTestCase {
  public void testEmptyHeader() {
    doTest("Import-Package: ",

           """
             ManifestFile:MANIFEST.MF
               Section
                 Header:Import-Package
                   ManifestToken:HEADER_NAME_TOKEN('Import-Package')
                   ManifestToken:COLON_TOKEN(':')
                   ManifestToken:SIGNIFICANT_SPACE_TOKEN(' ')
             """);
  }

  public void testSimpleClause() {
    doTest("Import-Package: com.acme\n",

           """
             ManifestFile:MANIFEST.MF
               Section
                 Header:Import-Package
                   ManifestToken:HEADER_NAME_TOKEN('Import-Package')
                   ManifestToken:COLON_TOKEN(':')
                   ManifestToken:SIGNIFICANT_SPACE_TOKEN(' ')
                   Clause
                     HeaderValuePart
                       ManifestToken:HEADER_VALUE_PART_TOKEN('com.acme')
                       ManifestToken:NEWLINE_TOKEN('\\n')
             """);
  }

  public void testEmptyClause() {
    doTest("Import-Package: ,com.acme\n",

           """
             ManifestFile:MANIFEST.MF
               Section
                 Header:Import-Package
                   ManifestToken:HEADER_NAME_TOKEN('Import-Package')
                   ManifestToken:COLON_TOKEN(':')
                   ManifestToken:SIGNIFICANT_SPACE_TOKEN(' ')
                   Clause
                     HeaderValuePart
                       <empty list>
                   ManifestToken:COMMA_TOKEN(',')
                   Clause
                     HeaderValuePart
                       ManifestToken:HEADER_VALUE_PART_TOKEN('com.acme')
                       ManifestToken:NEWLINE_TOKEN('\\n')
             """);
  }

  public void testAttribute() {
    doTest("Import-Package: com.acme;version=\"1.0.0\"\n",

           """
             ManifestFile:MANIFEST.MF
               Section
                 Header:Import-Package
                   ManifestToken:HEADER_NAME_TOKEN('Import-Package')
                   ManifestToken:COLON_TOKEN(':')
                   ManifestToken:SIGNIFICANT_SPACE_TOKEN(' ')
                   Clause
                     HeaderValuePart
                       ManifestToken:HEADER_VALUE_PART_TOKEN('com.acme')
                     ManifestToken:SEMICOLON_TOKEN(';')
                     Attribute:version
                       HeaderValuePart
                         ManifestToken:HEADER_VALUE_PART_TOKEN('version')
                       ManifestToken:EQUALS_TOKEN('=')
                       HeaderValuePart
                         ManifestToken:QUOTE_TOKEN('"')
                         ManifestToken:HEADER_VALUE_PART_TOKEN('1.0.0')
                         ManifestToken:QUOTE_TOKEN('"')
                         ManifestToken:NEWLINE_TOKEN('\\n')
             """);
  }

  public void testAttributeWithoutName() {
    doTest("Import-Package: com.acme;=\"1.0.0\"\n",

           """
             ManifestFile:MANIFEST.MF
               Section
                 Header:Import-Package
                   ManifestToken:HEADER_NAME_TOKEN('Import-Package')
                   ManifestToken:COLON_TOKEN(':')
                   ManifestToken:SIGNIFICANT_SPACE_TOKEN(' ')
                   Clause
                     HeaderValuePart
                       ManifestToken:HEADER_VALUE_PART_TOKEN('com.acme')
                     ManifestToken:SEMICOLON_TOKEN(';')
                     Attribute:
                       HeaderValuePart
                         <empty list>
                       ManifestToken:EQUALS_TOKEN('=')
                       HeaderValuePart
                         ManifestToken:QUOTE_TOKEN('"')
                         ManifestToken:HEADER_VALUE_PART_TOKEN('1.0.0')
                         ManifestToken:QUOTE_TOKEN('"')
                         ManifestToken:NEWLINE_TOKEN('\\n')
             """);
  }

  public void testAttributeOutsideParameter() {
    doTest("Import-Package: ;version=\"1.0.0\"\n",

           """
             ManifestFile:MANIFEST.MF
               Section
                 Header:Import-Package
                   ManifestToken:HEADER_NAME_TOKEN('Import-Package')
                   ManifestToken:COLON_TOKEN(':')
                   ManifestToken:SIGNIFICANT_SPACE_TOKEN(' ')
                   Clause
                     HeaderValuePart
                       <empty list>
                     ManifestToken:SEMICOLON_TOKEN(';')
                     Attribute:version
                       HeaderValuePart
                         ManifestToken:HEADER_VALUE_PART_TOKEN('version')
                       ManifestToken:EQUALS_TOKEN('=')
                       HeaderValuePart
                         ManifestToken:QUOTE_TOKEN('"')
                         ManifestToken:HEADER_VALUE_PART_TOKEN('1.0.0')
                         ManifestToken:QUOTE_TOKEN('"')
                         ManifestToken:NEWLINE_TOKEN('\\n')
             """);
  }

  public void testAttributeWithoutNameOutsideParameter() {
    doTest("Import-Package: =\"1.0.0\"\n",

           """
             ManifestFile:MANIFEST.MF
               Section
                 Header:Import-Package
                   ManifestToken:HEADER_NAME_TOKEN('Import-Package')
                   ManifestToken:COLON_TOKEN(':')
                   ManifestToken:SIGNIFICANT_SPACE_TOKEN(' ')
                   Clause
                     Attribute:
                       HeaderValuePart
                         <empty list>
                       ManifestToken:EQUALS_TOKEN('=')
                       HeaderValuePart
                         ManifestToken:QUOTE_TOKEN('"')
                         ManifestToken:HEADER_VALUE_PART_TOKEN('1.0.0')
                         ManifestToken:QUOTE_TOKEN('"')
                         ManifestToken:NEWLINE_TOKEN('\\n')
             """);
  }

  public void testDirective() {
    doTest("Import-Package: com.acme;resolution:=optional\n",

           """
             ManifestFile:MANIFEST.MF
               Section
                 Header:Import-Package
                   ManifestToken:HEADER_NAME_TOKEN('Import-Package')
                   ManifestToken:COLON_TOKEN(':')
                   ManifestToken:SIGNIFICANT_SPACE_TOKEN(' ')
                   Clause
                     HeaderValuePart
                       ManifestToken:HEADER_VALUE_PART_TOKEN('com.acme')
                     ManifestToken:SEMICOLON_TOKEN(';')
                     Directive:resolution
                       HeaderValuePart
                         ManifestToken:HEADER_VALUE_PART_TOKEN('resolution')
                       ManifestToken:COLON_TOKEN(':')
                       ManifestToken:EQUALS_TOKEN('=')
                       HeaderValuePart
                         ManifestToken:HEADER_VALUE_PART_TOKEN('optional')
                         ManifestToken:NEWLINE_TOKEN('\\n')
             """);
  }

  public void testDirectiveWithoutName() {
    doTest("Import-Package: com.acme;:=optional\n",

           """
             ManifestFile:MANIFEST.MF
               Section
                 Header:Import-Package
                   ManifestToken:HEADER_NAME_TOKEN('Import-Package')
                   ManifestToken:COLON_TOKEN(':')
                   ManifestToken:SIGNIFICANT_SPACE_TOKEN(' ')
                   Clause
                     HeaderValuePart
                       ManifestToken:HEADER_VALUE_PART_TOKEN('com.acme')
                     ManifestToken:SEMICOLON_TOKEN(';')
                     Directive:
                       HeaderValuePart
                         <empty list>
                       ManifestToken:COLON_TOKEN(':')
                       ManifestToken:EQUALS_TOKEN('=')
                       HeaderValuePart
                         ManifestToken:HEADER_VALUE_PART_TOKEN('optional')
                         ManifestToken:NEWLINE_TOKEN('\\n')
             """);
  }

  public void testDirectiveWithoutNameOutsideParameter() {
    doTest("Import-Package: :=optional\n",

           """
             ManifestFile:MANIFEST.MF
               Section
                 Header:Import-Package
                   ManifestToken:HEADER_NAME_TOKEN('Import-Package')
                   ManifestToken:COLON_TOKEN(':')
                   ManifestToken:SIGNIFICANT_SPACE_TOKEN(' ')
                   Clause
                     Directive:
                       HeaderValuePart
                         <empty list>
                       ManifestToken:COLON_TOKEN(':')
                       ManifestToken:EQUALS_TOKEN('=')
                       HeaderValuePart
                         ManifestToken:HEADER_VALUE_PART_TOKEN('optional')
                         ManifestToken:NEWLINE_TOKEN('\\n')
             """);
  }

  public void testDirectiveInvalidAssignmentTokens() {
    doTest("Import-Package: com.acme;resolution: =optional\n",

           """
             ManifestFile:MANIFEST.MF
               Section
                 Header:Import-Package
                   ManifestToken:HEADER_NAME_TOKEN('Import-Package')
                   ManifestToken:COLON_TOKEN(':')
                   ManifestToken:SIGNIFICANT_SPACE_TOKEN(' ')
                   Clause
                     HeaderValuePart
                       ManifestToken:HEADER_VALUE_PART_TOKEN('com.acme')
                     ManifestToken:SEMICOLON_TOKEN(';')
                     Directive:resolution
                       HeaderValuePart
                         ManifestToken:HEADER_VALUE_PART_TOKEN('resolution')
                       ManifestToken:COLON_TOKEN(':')
                       HeaderValuePart
                         ManifestToken:HEADER_VALUE_PART_TOKEN(' ')
                         ManifestToken:EQUALS_TOKEN('=')
                         ManifestToken:HEADER_VALUE_PART_TOKEN('optional')
                         ManifestToken:NEWLINE_TOKEN('\\n')
             """);
  }

  public void testSplitDirectiveAtColon() {
    doTest("""
             Import-Package: com.acme;resolution:
              =optional
             """,

           """
             ManifestFile:MANIFEST.MF
               Section
                 Header:Import-Package
                   ManifestToken:HEADER_NAME_TOKEN('Import-Package')
                   ManifestToken:COLON_TOKEN(':')
                   ManifestToken:SIGNIFICANT_SPACE_TOKEN(' ')
                   Clause
                     HeaderValuePart
                       ManifestToken:HEADER_VALUE_PART_TOKEN('com.acme')
                     ManifestToken:SEMICOLON_TOKEN(';')
                     Directive:resolution
                       HeaderValuePart
                         ManifestToken:HEADER_VALUE_PART_TOKEN('resolution')
                       ManifestToken:COLON_TOKEN(':')
                       ManifestToken:NEWLINE_TOKEN('\\n')
                       ManifestToken:SIGNIFICANT_SPACE_TOKEN(' ')
                       ManifestToken:EQUALS_TOKEN('=')
                       HeaderValuePart
                         ManifestToken:HEADER_VALUE_PART_TOKEN('optional')
                         ManifestToken:NEWLINE_TOKEN('\\n')
             """);
  }

  public void testAttributeAndDirective() {
    doTest("Import-Package: com.acme;version=\"1.0.0\";resolution:=optional\n",

           """
             ManifestFile:MANIFEST.MF
               Section
                 Header:Import-Package
                   ManifestToken:HEADER_NAME_TOKEN('Import-Package')
                   ManifestToken:COLON_TOKEN(':')
                   ManifestToken:SIGNIFICANT_SPACE_TOKEN(' ')
                   Clause
                     HeaderValuePart
                       ManifestToken:HEADER_VALUE_PART_TOKEN('com.acme')
                     ManifestToken:SEMICOLON_TOKEN(';')
                     Attribute:version
                       HeaderValuePart
                         ManifestToken:HEADER_VALUE_PART_TOKEN('version')
                       ManifestToken:EQUALS_TOKEN('=')
                       HeaderValuePart
                         ManifestToken:QUOTE_TOKEN('"')
                         ManifestToken:HEADER_VALUE_PART_TOKEN('1.0.0')
                         ManifestToken:QUOTE_TOKEN('"')
                     ManifestToken:SEMICOLON_TOKEN(';')
                     Directive:resolution
                       HeaderValuePart
                         ManifestToken:HEADER_VALUE_PART_TOKEN('resolution')
                       ManifestToken:COLON_TOKEN(':')
                       ManifestToken:EQUALS_TOKEN('=')
                       HeaderValuePart
                         ManifestToken:HEADER_VALUE_PART_TOKEN('optional')
                         ManifestToken:NEWLINE_TOKEN('\\n')
             """);
  }

  public void testAttributeAndDirectiveWithContinuationBeforeSemicolon() {
    doTest("""
             Import-Package: com.acme;version="1.0.0"
              ;resolution:=optional
             """,

           """
             ManifestFile:MANIFEST.MF
               Section
                 Header:Import-Package
                   ManifestToken:HEADER_NAME_TOKEN('Import-Package')
                   ManifestToken:COLON_TOKEN(':')
                   ManifestToken:SIGNIFICANT_SPACE_TOKEN(' ')
                   Clause
                     HeaderValuePart
                       ManifestToken:HEADER_VALUE_PART_TOKEN('com.acme')
                     ManifestToken:SEMICOLON_TOKEN(';')
                     Attribute:version
                       HeaderValuePart
                         ManifestToken:HEADER_VALUE_PART_TOKEN('version')
                       ManifestToken:EQUALS_TOKEN('=')
                       HeaderValuePart
                         ManifestToken:QUOTE_TOKEN('"')
                         ManifestToken:HEADER_VALUE_PART_TOKEN('1.0.0')
                         ManifestToken:QUOTE_TOKEN('"')
                         ManifestToken:NEWLINE_TOKEN('\\n')
                         ManifestToken:SIGNIFICANT_SPACE_TOKEN(' ')
                     ManifestToken:SEMICOLON_TOKEN(';')
                     Directive:resolution
                       HeaderValuePart
                         ManifestToken:HEADER_VALUE_PART_TOKEN('resolution')
                       ManifestToken:COLON_TOKEN(':')
                       ManifestToken:EQUALS_TOKEN('=')
                       HeaderValuePart
                         ManifestToken:HEADER_VALUE_PART_TOKEN('optional')
                         ManifestToken:NEWLINE_TOKEN('\\n')
             """);
  }

  public void testAttributeAndDirectiveWithContinuationAfterSemicolon() {
    doTest("""
             Import-Package: com.acme;version="1.0.0";
              resolution:=optional
             """,

           """
             ManifestFile:MANIFEST.MF
               Section
                 Header:Import-Package
                   ManifestToken:HEADER_NAME_TOKEN('Import-Package')
                   ManifestToken:COLON_TOKEN(':')
                   ManifestToken:SIGNIFICANT_SPACE_TOKEN(' ')
                   Clause
                     HeaderValuePart
                       ManifestToken:HEADER_VALUE_PART_TOKEN('com.acme')
                     ManifestToken:SEMICOLON_TOKEN(';')
                     Attribute:version
                       HeaderValuePart
                         ManifestToken:HEADER_VALUE_PART_TOKEN('version')
                       ManifestToken:EQUALS_TOKEN('=')
                       HeaderValuePart
                         ManifestToken:QUOTE_TOKEN('"')
                         ManifestToken:HEADER_VALUE_PART_TOKEN('1.0.0')
                         ManifestToken:QUOTE_TOKEN('"')
                     ManifestToken:SEMICOLON_TOKEN(';')
                     Directive:resolution
                       HeaderValuePart
                         ManifestToken:NEWLINE_TOKEN('\\n')
                         ManifestToken:SIGNIFICANT_SPACE_TOKEN(' ')
                         ManifestToken:HEADER_VALUE_PART_TOKEN('resolution')
                       ManifestToken:COLON_TOKEN(':')
                       ManifestToken:EQUALS_TOKEN('=')
                       HeaderValuePart
                         ManifestToken:HEADER_VALUE_PART_TOKEN('optional')
                         ManifestToken:NEWLINE_TOKEN('\\n')
             """);
  }

  public void testAttributeAndDirectiveWithBadContinuationAfterAttributeName() {
    doTest("""
             Import-Package: com.acme;attr
             =attr_value;dir:=dir_value
             """,

           """
             ManifestFile:MANIFEST.MF
               Section
                 Header:Import-Package
                   ManifestToken:HEADER_NAME_TOKEN('Import-Package')
                   ManifestToken:COLON_TOKEN(':')
                   ManifestToken:SIGNIFICANT_SPACE_TOKEN(' ')
                   Clause
                     HeaderValuePart
                       ManifestToken:HEADER_VALUE_PART_TOKEN('com.acme')
                     ManifestToken:SEMICOLON_TOKEN(';')
                     HeaderValuePart
                       ManifestToken:HEADER_VALUE_PART_TOKEN('attr')
                       ManifestToken:NEWLINE_TOKEN('\\n')
                 Header:=attr_value;dir
                   ManifestToken:HEADER_NAME_TOKEN('=attr_value;dir')
                   ManifestToken:COLON_TOKEN(':')
                   PsiErrorElement:Whitespace expected
                     <empty list>
                   HeaderValuePart
                     ManifestToken:EQUALS_TOKEN('=')
                     ManifestToken:HEADER_VALUE_PART_TOKEN('dir_value')
                     ManifestToken:NEWLINE_TOKEN('\\n')
             """);
  }

  public void testTwoClauses() {
    doTest("Import-Package: com.acme.p;a=b,com.acme\n",

           """
             ManifestFile:MANIFEST.MF
               Section
                 Header:Import-Package
                   ManifestToken:HEADER_NAME_TOKEN('Import-Package')
                   ManifestToken:COLON_TOKEN(':')
                   ManifestToken:SIGNIFICANT_SPACE_TOKEN(' ')
                   Clause
                     HeaderValuePart
                       ManifestToken:HEADER_VALUE_PART_TOKEN('com.acme.p')
                     ManifestToken:SEMICOLON_TOKEN(';')
                     Attribute:a
                       HeaderValuePart
                         ManifestToken:HEADER_VALUE_PART_TOKEN('a')
                       ManifestToken:EQUALS_TOKEN('=')
                       HeaderValuePart
                         ManifestToken:HEADER_VALUE_PART_TOKEN('b')
                   ManifestToken:COMMA_TOKEN(',')
                   Clause
                     HeaderValuePart
                       ManifestToken:HEADER_VALUE_PART_TOKEN('com.acme')
                       ManifestToken:NEWLINE_TOKEN('\\n')
             """);
  }

  public void testVersionRange() {
    doTest("Import-Package: com.acme.p;version=\"(1.2.3,2.4.3]\",com.acme.l;version=\"[1.2.4,2.3.4)\"\n",

           """
             ManifestFile:MANIFEST.MF
               Section
                 Header:Import-Package
                   ManifestToken:HEADER_NAME_TOKEN('Import-Package')
                   ManifestToken:COLON_TOKEN(':')
                   ManifestToken:SIGNIFICANT_SPACE_TOKEN(' ')
                   Clause
                     HeaderValuePart
                       ManifestToken:HEADER_VALUE_PART_TOKEN('com.acme.p')
                     ManifestToken:SEMICOLON_TOKEN(';')
                     Attribute:version
                       HeaderValuePart
                         ManifestToken:HEADER_VALUE_PART_TOKEN('version')
                       ManifestToken:EQUALS_TOKEN('=')
                       HeaderValuePart
                         ManifestToken:QUOTE_TOKEN('"')
                         ManifestToken:OPENING_PARENTHESIS_TOKEN('(')
                         ManifestToken:HEADER_VALUE_PART_TOKEN('1.2.3')
                         ManifestToken:COMMA_TOKEN(',')
                         ManifestToken:HEADER_VALUE_PART_TOKEN('2.4.3')
                         ManifestToken:CLOSING_BRACKET_TOKEN(']')
                         ManifestToken:QUOTE_TOKEN('"')
                   ManifestToken:COMMA_TOKEN(',')
                   Clause
                     HeaderValuePart
                       ManifestToken:HEADER_VALUE_PART_TOKEN('com.acme.l')
                     ManifestToken:SEMICOLON_TOKEN(';')
                     Attribute:version
                       HeaderValuePart
                         ManifestToken:HEADER_VALUE_PART_TOKEN('version')
                       ManifestToken:EQUALS_TOKEN('=')
                       HeaderValuePart
                         ManifestToken:QUOTE_TOKEN('"')
                         ManifestToken:OPENING_BRACKET_TOKEN('[')
                         ManifestToken:HEADER_VALUE_PART_TOKEN('1.2.4')
                         ManifestToken:COMMA_TOKEN(',')
                         ManifestToken:HEADER_VALUE_PART_TOKEN('2.3.4')
                         ManifestToken:CLOSING_PARENTHESIS_TOKEN(')')
                         ManifestToken:QUOTE_TOKEN('"')
                         ManifestToken:NEWLINE_TOKEN('\\n')
             """);
  }

  private void doTest(String source, String expected) {
    PsiFile file = createLightFile("MANIFEST.MF", source);
    assertEquals(expected, DebugUtil.psiToString(file, false));
  }
}
