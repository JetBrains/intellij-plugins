package com.intellij.flex.refactoring;

import com.intellij.codeInsight.EditorInfo;
import com.intellij.flex.util.FlexTestUtils;
import com.intellij.ide.util.DirectoryUtil;
import com.intellij.javascript.flex.css.FlexStylesIndexableSetContributor;
import com.intellij.javascript.flex.refactoring.moveClass.FlexMoveFileRefactoringHandler;
import com.intellij.javascript.flex.refactoring.moveClass.FlexMoveInnerClassProcessor;
import com.intellij.lang.javascript.JSTestOption;
import com.intellij.lang.javascript.JSTestOptions;
import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.flex.FlexModuleType;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.ecmal4.JSQualifiedNamedElement;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.module.ModuleType;
import com.intellij.openapi.roots.ModuleRootManager;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.newvfs.impl.VfsRootAccess;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.refactoring.BaseRefactoringProcessor;
import com.intellij.refactoring.MultiFileTestCase;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;

import static com.intellij.openapi.vfs.VfsUtilCore.convertFromUrl;
import static com.intellij.openapi.vfs.VfsUtilCore.urlToPath;

public class FlexMoveInnerClassTest extends MultiFileTestCase {

  @NotNull
  @Override
  protected String getTestRoot() {
    return "move_inner/";
  }

  @Override
  protected String getTestDataPath() {
    return FlexTestUtils.getTestDataPath("");
  }

  @Override
  protected void setUp() throws Exception {
    VfsRootAccess.allowRootAccess(getTestRootDisposable(),
                                  urlToPath(convertFromUrl(FlexStylesIndexableSetContributor.class.getResource("FlexStyles.as"))));
    super.setUp();
    JSTestUtils.disableFileHeadersInTemplates(getProject());
  }

  @Override
  protected ModuleType getModuleType() {
    return FlexModuleType.getInstance();
  }

  @Override
  protected void setUpJdk() {
    FlexTestUtils.setupFlexSdk(myModule, getTestName(false), getClass());
  }

  private void doTest(final String fromFilePath,
                      final String className,
                      final String targetPackage,
                      final boolean searchInStringsAndComments,
                      final boolean searchTextOccurences,
                      final String[] conflicts) throws Exception {
    doTest(new PerformAction() {
      @Override
      public void performAction(VirtualFile rootDir, VirtualFile rootAfter) throws Exception {
        FlexMoveInnerClassTest.this
          .performAction(rootDir, fromFilePath, className, targetPackage, searchInStringsAndComments, searchTextOccurences, conflicts);
      }
    }, false);
  }

  private void performAction(VirtualFile rootDir,
                             String fromFilePath,
                             String className,
                             final String targetPackage,
                             boolean searchInStringsAndComments,
                             boolean searchTextOccurences,
                             String[] conflicts) {
    PsiFile sourceFile = myPsiManager.findFile(rootDir.findFileByRelativePath(fromFilePath));
    final EditorInfo editorInfo = new EditorInfo(sourceFile.getText());
    assertEquals("Wrong number of carets", 1, editorInfo.caretState.carets.size());
    assertNotNull("No caret specified", editorInfo.caretState.carets.get(0).position);
    final Document doc = PsiDocumentManager.getInstance(myProject).getDocument(sourceFile);

    final PsiDirectory targetDirectory = WriteAction.compute(() -> {
      doc.setText(editorInfo.getNewFileText());
      PsiDocumentManager.getInstance(myProject).commitDocument(doc);

      final VirtualFile srcRootFile = ModuleRootManager.getInstance(myModule).getSourceRoots()[0];
      final VirtualFile file = VfsUtilCore.findRelativeFile(targetPackage.replace('.', File.separatorChar), srcRootFile);
      if (file != null) {
        return myPsiManager.findDirectory(file);
      }
      else {
        PsiDirectory srcRoot = myPsiManager.findDirectory(srcRootFile);
        return DirectoryUtil.createSubdirectories(targetPackage, srcRoot, ".");
      }
    });

    PsiElement element = sourceFile.findElementAt(editorInfo.caretState.carets.get(0).getCaretOffset(doc));

    while (true) {
      assertFalse("inner element to move not found", element instanceof JSFile);
      final JSQualifiedNamedElement adjusted = FlexMoveFileRefactoringHandler.adjustForMove(element);
      if (adjusted != null) {
        element = adjusted;
        break;
      }
      element = element.getParent();
    }

    try {

      new FlexMoveInnerClassProcessor((JSQualifiedNamedElement)element, targetDirectory, className, targetPackage,
                                      searchInStringsAndComments, searchTextOccurences, null).run();
      assertEquals("Conflicts expected:\n" + StringUtil.join(conflicts, "\n"), 0, conflicts.length);
    }
    catch (BaseRefactoringProcessor.ConflictsInTestsException e) {
      assertTrue("Conflicts not expected but found:" + e.getMessage(), conflicts.length > 0);
      assertSameElements(e.getMessages(), conflicts);
      myDoCompare = false;
    }
  }

  @JSTestOptions({JSTestOption.WithFlexSdk})
  public void testClass() throws Exception {
    doTest("/From.as", "Foo2", "com.foo", true, false, ArrayUtil.EMPTY_STRING_ARRAY);
  }

  // IDEA-66103
  //public void testTextUsagesInInjection() throws Exception {
  //  doTest("/From.as", "bar", "com.foo", true, true, ArrayUtil.EMPTY_STRING_ARRAY);
  //}

  public void testFunction() throws Exception {
    doTest("/a/From.as", "bar", "a", false, false, ArrayUtil.EMPTY_STRING_ARRAY);
  }

  public void testVariable() throws Exception {
    doTest("/a/From.as", "myVar", "a", false, false, ArrayUtil.EMPTY_STRING_ARRAY);
  }

  public void testFunction2() throws Exception {
    doTest("/a/From.as", "bar", "b", false, false, ArrayUtil.EMPTY_STRING_ARRAY);
  }

  public void testConflicts1() throws Exception {
    String[] conflicts = new String[]{
      "Inner function bar() won't be accessible from inner function foo()"
    };
    doTest("/From.as", "foo", "", false, false, conflicts);
  }

  public void testClass2() throws Exception {
    doTest("/A.as", "B", "", true, false, ArrayUtil.EMPTY_STRING_ARRAY);
  }

  public void testNoPackageStatement() throws Exception {
    doTest("/A.as", "my", "foo", true, false, ArrayUtil.EMPTY_STRING_ARRAY);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testGenericVectorCreation() throws Exception {
    doTest("/Z1.as", "Cell", "", true, false, ArrayUtil.EMPTY_STRING_ARRAY);
  }

  @JSTestOptions({JSTestOption.WithFlexFacet, JSTestOption.WithGumboSdk})
  public void testClassFromDefaultPackageConstructorCall() throws Exception {
    doTest("/Z1.as", "Foo", "", true, false, ArrayUtil.EMPTY_STRING_ARRAY);
  }
}
