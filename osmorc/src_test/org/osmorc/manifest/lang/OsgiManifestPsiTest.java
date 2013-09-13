package org.osmorc.manifest.lang;

import com.intellij.psi.PsiFile;
import com.intellij.testFramework.LightIdeaTestCase;
import org.jetbrains.lang.manifest.header.HeaderParserRepository;
import org.jetbrains.lang.manifest.psi.Header;
import org.jetbrains.lang.manifest.psi.HeaderValue;
import org.jetbrains.lang.manifest.psi.ManifestFile;
import org.osgi.framework.Version;
import org.osmorc.manifest.lang.psi.AssignmentExpression;
import org.osmorc.manifest.lang.psi.Clause;

import java.util.List;

public class OsgiManifestPsiTest extends LightIdeaTestCase {
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

  private static ManifestFile createFile(String text) {
    PsiFile file = createLightFile("MANIFEST.MF", text);
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
