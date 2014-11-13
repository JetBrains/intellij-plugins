package com.intellij.aws.cloudformation.tests;

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiRecursiveElementVisitor;
import com.intellij.psi.PsiReference;
import com.intellij.testFramework.ResolveTestCase;
import com.intellij.util.ObjectUtils;
import org.junit.Assert;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public abstract class ResolveTestsBase extends ResolveTestCase {
  protected Object NotResolved = new Object();
  private Class<?> myReferenceClass;

  protected ResolveTestsBase(Class<?> referenceClass) {
    myReferenceClass = referenceClass;
  }

  protected void assertEntityResolve(String testName, Object... entityNames) throws Exception {
    final String templateFileName = testName + ".template";

    FileUtil.copy(new File(getTestDataPath(), templateFileName), new File(myProject.getBasePath(), templateFileName));

    final VirtualFile vFile = myProject.getBaseDir().findChild(templateFileName);
    assertNotNull(vFile);

    String fileText = StringUtil.convertLineSeparators(VfsUtil.loadText(vFile));

    final String fileName = vFile.getName();

    List<Integer> offsets = new ArrayList<Integer>();
    while (true) {
      int offset = fileText.indexOf(MARKER);
      if (offset < 0) {
        break;
      }

      fileText = fileText.substring(0, offset) + fileText.substring(offset + MARKER.length());
      offsets.add(offset);
    }

    Assert.assertTrue("Test input must contain one or more " + MARKER + "markers", offsets.size() > 0);
    Assert.assertEquals("Number of assertions and markers differs", entityNames.length, offsets.size());

    myFile = createFile(myModule, fileName, fileText);
    Assert.assertFalse(
        "File " + myFile.getName() + " contains error(s)",
        ErrorUtil.containsError(myFile));

    for (int i = 0; i < offsets.size(); i++) {
      PsiReference ref = myFile.findReferenceAt(offsets.get(i));
      assertNotNull("Reference not found at marker #" + (i + 1), ref);

      Assert.assertTrue("Ref should be " + myReferenceClass.getName() +
              ", but got " + ref.getClass().getName(),
          myReferenceClass.isAssignableFrom(ref.getClass()));

      final PsiElement resolved = ref.resolve();
      final Object expectedEntity = entityNames[i];

      if (expectedEntity == NotResolved) {
        if (resolved != null) {
          Assert.fail("Ref #" + (i + 1) + " should be unresolved, but resolved to " + resolved.getText());
        }
      } else {
        if (resolved == null) {
          Assert.fail("Ref #" + (i + 1) + " is unresolved");
        }

        final PsiNamedElement namedResolved = ObjectUtils.tryCast(resolved, PsiNamedElement.class);
        if (namedResolved == null) {
          Assert.fail("Ref #" + (i + 1) + " should be named element");
        }

        if (!expectedEntity.equals(namedResolved.getName())) {
          Assert.fail("Wrong resolve result, should resolve to " + expectedEntity + ", but got " + namedResolved.getName());
        }
      }
    }

    final ReferencesCollectorElementVisitor visitor = new ReferencesCollectorElementVisitor();
    myFile.accept(visitor);

    if (visitor.myResult.size() > entityNames.length) {
      Assert.fail("More references found in file");
    } else if (visitor.myResult.size() < entityNames.length) {
      Assert.fail("Less references found in file?");
    }
  }

  private class ReferencesCollectorElementVisitor extends PsiRecursiveElementVisitor {
    private List<PsiReference> myResult = new ArrayList<PsiReference>();

    public void visitElement(PsiElement element) {
      super.visitElement(element);

      for (PsiReference reference : element.getReferences()) {
        if (!myReferenceClass.isAssignableFrom(reference.getClass())) {
          continue;
        }

        myResult.add(reference);
      }
    }
  }
}
