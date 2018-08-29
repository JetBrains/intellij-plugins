// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.osmorc.manifest.lang;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import org.jetbrains.lang.manifest.header.HeaderParserRepository;
import org.jetbrains.lang.manifest.psi.Header;
import org.jetbrains.lang.manifest.psi.HeaderValue;
import org.jetbrains.lang.manifest.psi.ManifestFile;
import org.osgi.framework.Version;
import org.osmorc.LightOsgiFixtureTestCase;
import org.osmorc.manifest.lang.psi.AssignmentExpression;
import org.osmorc.manifest.lang.psi.Clause;

import java.util.List;

public class OsgiManifestPsiTest extends LightOsgiFixtureTestCase {
  public void testClauses() {
    ManifestFile file = createFile("Import-Package: a.b,c.d;a=value;d:=value");
    Header header = file.getHeader("Import-Package");
    assertNotNull(header);
    List<HeaderValue> clauses = header.getHeaderValues();
    assertEquals(2, clauses.size());
    Clause clause1 = (Clause)clauses.get(0), clause2 = (Clause)clauses.get(1);
    assertEquals(0, clause1.getAttributes().size());
    assertEquals(0, clause1.getDirectives().size());
    assertEquals(1, clause2.getAttributes().size());
    assertEquals(1, clause2.getDirectives().size());
    assertNotNull(clause2.getAttribute("a"));
    assertNull(clause2.getAttribute("b"));
    assertNotNull(clause2.getDirective("d"));
    assertNull(clause2.getDirective("z"));
  }

  public void testAttributes() {
    ManifestFile file = createFile("Import-Package: com.acme;a1=value1;a2=value2");
    assertAssignment(file, true, "a1", "value1");
    assertAssignment(file, true, "a2", "value2");
    assertAssignment(file, true, "a3", null);
    assertAssignment(file, false, "a1", null);
  }

  public void testDirectives() {
    ManifestFile file = createFile("Import-Package: com.acme;d1:=value1;d2:=value2");
    assertAssignment(file, false, "d1", "value1");
    assertAssignment(file, false, "d2", "value2");
    assertAssignment(file, false, "d3", null);
    assertAssignment(file, true, "d1", null);
  }

  public void testBundleVersion() {
    ManifestFile file = createFile("Bundle-Version: 1.2.3.b300\n");
    Header header = file.getHeader("Bundle-Version");
    assertNotNull(header);
    Object value = HeaderParserRepository.getInstance().getConvertedValue(header);
    assertEquals(new Version(1, 2, 3, "b300"), value);
  }

  public void testBundleReference() {
    ManifestFile file = createFile("Require-Bundle: biz.aQute.repository\n");
    PsiReference ref = file.findReferenceAt(16);
    assertNotNull(ref);
    PsiElement target = ref.resolve();
    assertTrue(String.valueOf(target), target instanceof Header);
    assertEquals("Bundle-SymbolicName", ((Header)target).getName());
  }

  private ManifestFile createFile(String text) {
    PsiFile file = myFixture.addFileToProject("MANIFEST.MF", text);
    assert file instanceof ManifestFile : file;
    return (ManifestFile)file;
  }

  private static void assertAssignment(ManifestFile file, boolean attribute, String name, String expected) {
    List<Header> headers = file.getHeaders();
    assertEquals(1, headers.size());
    List<HeaderValue> clauses = headers.get(0).getHeaderValues();
    assertEquals(1, clauses.size());
    assertTrue(clauses.get(0) instanceof Clause);
    Clause clause = (Clause)clauses.get(0);
    AssignmentExpression element = attribute ? clause.getAttribute(name) : clause.getDirective(name);
    if (expected != null) {
      assertNotNull(element);
      assertEquals(expected, element.getValue());
    }
    else {
      assertNull(element);
    }
  }
}
