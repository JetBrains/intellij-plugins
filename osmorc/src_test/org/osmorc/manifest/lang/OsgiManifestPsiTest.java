package org.osmorc.manifest.lang;

import com.intellij.psi.PsiFile;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.testFramework.LightIdeaTestCase;
import org.jetbrains.lang.manifest.header.HeaderParserRepository;
import org.jetbrains.lang.manifest.psi.Header;
import org.jetbrains.lang.manifest.psi.ManifestFile;
import org.osmorc.manifest.lang.psi.AssignmentExpression;
import org.osmorc.manifest.lang.psi.Clause;
import org.osmorc.valueobject.Version;

import java.util.List;

public class OsgiManifestPsiTest extends LightIdeaTestCase {
  public void testClauses() {
    ManifestFile file = createFile("Import-Package: a.b,c.d;a=value;d:=value");
    Header header = file.getHeader("Import-Package");
    assertNotNull(header);
    List<Clause> clauses = PsiTreeUtil.getChildrenOfTypeAsList(header, Clause.class);
    assertEquals(2, clauses.size());
    assertEquals(0, clauses.get(0).getAttributes().length);
    assertEquals(0, clauses.get(0).getDirectives().length);
    assertEquals(1, clauses.get(1).getAttributes().length);
    assertEquals(1, clauses.get(1).getDirectives().length);
    assertNotNull(clauses.get(1).getAttribute("a"));
    assertNull(clauses.get(1).getAttribute("b"));
    assertNotNull(clauses.get(1).getDirective("d"));
    assertNull(clauses.get(1).getDirective("z"));
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
    Header[] headers = file.getHeaders();
    assertEquals(1, headers.length);
    List<Clause> clauses = PsiTreeUtil.getChildrenOfTypeAsList(headers[0], Clause.class);
    assertEquals(1, clauses.size());
    AssignmentExpression element = attribute ? clauses.get(0).getAttribute(name) : clauses.get(0).getDirective(name);
    if (expected != null) {
      assertNotNull(element);
      assertEquals(expected, element.getValue());
    }
    else {
      assertNull(element);
    }
  }
}
