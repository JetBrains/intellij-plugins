package com.intellij.flex.uiDesigner;

import com.intellij.flex.model.bc.TargetPlatform;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.TripleFunction;
import gnu.trove.THashSet;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

@Flex(version="4.5")
public class MxmlTest extends MxmlTestBase {
  @Flex(platform=TargetPlatform.Mobile, version="4.6")
  public void testMobile() throws Exception {
    testFiles(DesignerTests.getFile("src/mobile"));
  }

  public void testResolveResourceIfNameIsAmbiguous() throws Exception {
    moduleInitializer = new TripleFunction<ModifiableRootModel, VirtualFile, List<String>, Void>() {
      @Override
      public Void fun(ModifiableRootModel model, VirtualFile file, List<String> libs) {
        final VirtualFile localesDir = DesignerTests.getFile("locales");
        final ContentEntry localesContentEntry = model.addContentEntry(localesDir);
        //noinspection ConstantConditions
        localesContentEntry.addSourceFolder(localesDir.findChild("en_US"), false);
        //localesContentEntry.addSourceFolder(localesDir.findChild("ru_RU"), false);
        return null;
      }
    };

    testFile("ResourceDirective.mxml");
  }

  private void init45And46Tests() {
    moduleInitializer = new TripleFunction<ModifiableRootModel, VirtualFile, List<String>, Void>() {
      @Override
      public Void fun(ModifiableRootModel model, VirtualFile sourceDir, List<String> libs) {
        libs.add(getFudHome() + "/test-data-helper/target/test-data-helper.swc");
        final VirtualFile assetsDir = DesignerTests.getFile("assets");
        model.addContentEntry(assetsDir).addSourceFolder(assetsDir, false);

        THashSet<ProblemDescriptor> expectedProblems = new THashSet<ProblemDescriptor>();
        TestDocumentProblemManager.setExpectedProblems(expectedProblems);
        expectedProblems.add(new ProblemDescriptor("spark.components.supportClasses.TrackBase is abstract class",
                                                   sourceDir.findFileByRelativePath("AbstractClass.mxml"), 3));
        expectedProblems.add(new ProblemDescriptor("Default property not found for Rect", sourceDir.findFileByRelativePath("CannotFindDefaultProperty.mxml"), 2));

        VirtualFile file = sourceDir.findFileByRelativePath("ClassProperty.mxml");
        expectedProblems.add(new ProblemDescriptor("Invalid class value", file, 6));
        expectedProblems.add(new ProblemDescriptor("Invalid class value", file, 11));

        expectedProblems.add(new ProblemDescriptor("Unsupported embed asset type \"@Embed(source='/jazz.mp3')\"", sourceDir.findFileByRelativePath("Embed.mxml"), 3));

        file = sourceDir.findFileByRelativePath("InvalidColorNameOrNumericValue.mxml");
        expectedProblems.add(new ProblemDescriptor("Invalid color name invalidcolorname", file, 2));
        expectedProblems.add(new ProblemDescriptor("Invalid numeric value", file, 3));
        expectedProblems.add(new ProblemDescriptor("Invalid numeric value", file, 4));
        expectedProblems.add(new ProblemDescriptor("Invalid numeric value", file, 5));
        expectedProblems.add(new ProblemDescriptor("Invalid numeric value", file, 6));

        //expectedProblems.add(new ProblemDescriptor("<a href=\"http://youtrack.jetbrains.net/issue/IDEA-72175\">Inline components are not supported</a>", sourceDir.findFileByRelativePath("ItemRendererAndMixDefaultExplicitContent.mxml"), 9));
        expectedProblems.add(new ProblemDescriptor("Unresolved variable or type unresolvedData", sourceDir.findFileByRelativePath("ArrayOfPrimitives.mxml"), 10));

        file = sourceDir.findFileByRelativePath("ChildrenTypeCheck.mxml");
        expectedProblems.add(new ProblemDescriptor("Initializer for Group cannot be represented in text", file, 2));
        expectedProblems.add(new ProblemDescriptor("Initializer for Container cannot be represented in text", file, 5));
        expectedProblems.add(new ProblemDescriptor("Children of Accordion must be mx.core.INavigatorContent", file, 8));
        return null;
      }
    };
  }

  public void test45() throws Exception {
    init45And46Tests();
    testFiles(getTestDir(), DesignerTests.getFile("src/mx"));
  }

  @Flex(version="4.6")
  public void test46() throws Exception {
    init45And46Tests();
    testFiles(getTestDir(), DesignerTests.getFile("src/mx"));
  }

  private void testFiles(VirtualFile... roots) throws Exception {
    Pair<VirtualFile[], VirtualFile[]> pair = computeFiles(getTestFiles(roots));
    testFiles(pair.first, pair.second);
  }

  @Flex(version="4.1")
  public void test41() throws Exception {
    testFile("states/UnusedStates.mxml");
  }

  @Override
  protected String expectedErrorForDocument(String documentName) {
     if (documentName.equals("RuntimeErrorInMxmlRead")) {
      return "Error: Boo\n\tat com.intellij.flex.uiDesigner.test::LabelWithError()";
    }
    else if (documentName.equals("RuntimeError")) {
      return "Error: I am runtime error\n\tat com.intellij.flex.uiDesigner.test::DateFieldWithError/commitProperties()";
    }

    return null;
  }

  private static Pair<VirtualFile[], VirtualFile[]> getTestFiles(VirtualFile... roots) {
    final ArrayList<VirtualFile> files = new ArrayList<VirtualFile>(128);
    final ArrayList<VirtualFile> auxFiles = new ArrayList<VirtualFile>(8);

    for (VirtualFile root : roots) {
      collectMxmlFiles(files, auxFiles, root);
    }

    final VirtualFile[] list = files.toArray(new VirtualFile[files.size()]);
    final VirtualFile[] auxList = auxFiles.toArray(new VirtualFile[auxFiles.size()]);

    final VirtualFileComparator virtualFileComparator = new VirtualFileComparator();
    Arrays.sort(list, virtualFileComparator);
    Arrays.sort(auxList, virtualFileComparator);
    
    return new Pair<VirtualFile[], VirtualFile[]>(list, auxList);
  }

  private Pair<VirtualFile[], VirtualFile[]> computeFiles(Pair<VirtualFile[], VirtualFile[]> pair) {
    final String testFile = System.getProperty("testFile");

    VirtualFile[] auxiliaryFiles = pair.getSecond();
    if (testFile == null) {
      return pair;
    }
    else {
      String[] strings = testFile.split(",");
      if (strings.length > 1) {
        auxiliaryFiles = new VirtualFile[auxiliaryFiles.length + strings.length - 1];
        System.arraycopy(pair.getSecond(), 0, auxiliaryFiles, 0, pair.getSecond().length);
        for (int i = 1, j = pair.getSecond().length, n = strings.length; i < n; i++) {
          auxiliaryFiles[j++] = getSource(strings[i] + ".mxml");
        }
      }

      return new Pair<VirtualFile[], VirtualFile[]>(new VirtualFile[]{getSource(strings[0] + ".mxml")}, auxiliaryFiles);
    }
  }

  private static void collectMxmlFiles(ArrayList<VirtualFile> files, ArrayList<VirtualFile> auxFiles, VirtualFile parent) {
    for (VirtualFile file : parent.getChildren()) {
      final String name = file.getName();
      if (name.charAt(0) == '.') {
        // skip
      }
      else if (name.startsWith("Aux")) {
        auxFiles.add(file);
      }
      else if (name.endsWith(JavaScriptSupportLoader.MXML_FILE_EXTENSION_DOT) &&
               !name.startsWith("T.") &&
               !name.startsWith("TestApp.") &&
               !name.startsWith("MigLayoutExample.") &&
               !name.startsWith("ResourceDirective.") &&
               //!name.startsWith("ProjectMxmlComponentAsParentWithDefaultProperty.") &&
               !name.startsWith("GenericMxmlSupport.")) {
        files.add(file);
      }

      if (file.isDirectory()) {
        collectMxmlFiles(files, auxFiles, file);
      }
    }
  }

  private static class VirtualFileComparator implements Comparator<VirtualFile> {
    @Override
    public int compare(VirtualFile o1, VirtualFile o2) {
      return o1.getPath().compareTo(o2.getPath());
    }
  }

  @Override
  protected void tearDown() throws Exception {
    TestDocumentProblemManager.setExpectedProblems(null);
    super.tearDown();
  }
}