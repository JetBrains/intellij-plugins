package com.intellij.flex.uiDesigner;

import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.projectStructure.model.TargetPlatform;
import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.StringBuilderSpinAllocator;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;

import static com.intellij.flex.uiDesigner.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.emptyArray;

@Flex(version="4.5")
public class MxmlTest extends MxmlTestBase {
  @Override
  protected void modifyModule(ModifiableRootModel model, VirtualFile rootDir, List<String> libs) {
    if (getName().equals("test45")) {
      libs.add(getFudHome() + "/test-data-helper/target/test-data-helper.swc");
      final VirtualFile assetsDir = getVFile("assets");
      model.addContentEntry(assetsDir).addSourceFolder(assetsDir, false);
    }
    else if (getName().equals("testResolveResourceIfNameIsAmbiguous")) {
      final VirtualFile localesDir = getVFile("locales");
      final ContentEntry localesContentEntry = model.addContentEntry(localesDir);
      //noinspection ConstantConditions
      localesContentEntry.addSourceFolder(localesDir.findChild("en_US"), false);
      //localesContentEntry.addSourceFolder(localesDir.findChild("ru_RU"), false);
    }
    else if (getName().equals("testMobile")) {
      libs.add("mobilecomponents-4.5.1.swc");
    }
  }

  @Flex(platform=TargetPlatform.Mobile, version="4.6")
  public void testMobile() throws Exception {
    testFiles(getVFile((getTestDataPath() + "/src/mobile")));
    assertThat(getLastProblems(), emptyArray());
  }

  public void testResolveResourceIfNameIsAmbiguous() throws Exception {
    testFile("ResourceDirective.mxml");
  }

  public void test45() throws Exception {
    testFiles(getTestDir(), getVFile(getTestDataPath() + "/src/mx"));
    
    String[] problems = getLastProblems();
    if (System.getProperty("testFile") != null) {
      assertThat(problems, emptyArray());
    }
    else {
      assertThat(problems,
                 m("spark.components.supportClasses.TrackBase is abstract class (line: 3)"),
                 m("Default property not found for Rect (line: 2)"),
                 m("Invalid class value (line: 6)", "Invalid class value (line: 11)"),
                 m("Unsupported embed asset type \"@Embed(source='/jazz.mp3')\" (line: 3)"),
                 m("Invalid color name invalidcolorname (line: 2)", "Invalid numeric value (line: 3)", "Invalid numeric value (line: 4)"),
                 m("<a href=\"http://youtrack.jetbrains.net/issue/IDEA-72175\">Inline components are not supported</a> (line: 9)"),
                 m("Unresolved variable or type unresolvedData (line: 10)"),
                 m("Initializer for Group cannot be represented in text (line: 2)",
                   "Initializer for Container cannot be represented in text (line: 5)",
                   "Children of Accordion must be mx.core.INavigatorContent (line: 8)")
      );
    }
  }

  @Flex(version="4.6")
  public void test46() throws Exception {
    testFiles(getTestDir(), getVFile(getTestDataPath() + "/src/mx"));

    String[] problems = getLastProblems();
    if (System.getProperty("testFile") != null) {
      assertThat(problems, emptyArray());
    }
    else {
      assertThat(problems,
                 m("spark.components.supportClasses.TrackBase is abstract class (line: 3)"),
                 m("Default property not found for Rect (line: 2)"),
                 m("Invalid class value (line: 6)", "Invalid class value (line: 11)"),
                 m("Unsupported embed asset type \"@Embed(source='/jazz.mp3')\" (line: 3)"),
                 m("Invalid color name invalidcolorname (line: 2)", "Invalid numeric value (line: 3)", "Invalid numeric value (line: 4)"),
                 m("<a href=\"http://youtrack.jetbrains.net/issue/IDEA-72175\">Inline components are not supported</a> (line: 9)"),
                 m("Unresolved variable or type unresolvedData (line: 10)"),
                 m("Initializer for Group cannot be represented in text (line: 2)",
                   "Initializer for Container cannot be represented in text (line: 5)",
                   "Children of Accordion must be mx.core.INavigatorContent (line: 8)")
      );
    }
  }

  private void testFiles(VirtualFile... roots) throws Exception {
    Pair<VirtualFile[], VirtualFile[]> pair = computeFiles(getTestFiles(roots));
    testFiles(pair.first, pair.second);
  }

  private static String m(String... messages) {
    final StringBuilder builder = StringBuilderSpinAllocator.alloc();
    try {
      builder.append("<b>Flex UI Designer</b><ul>");
      for (String message : messages) {
        builder.append("<li>").append(message).append("</li>");
      }
      builder.append("</ul>");
      return builder.toString();
    }
    finally {
      StringBuilderSpinAllocator.dispose(builder);
    }
  }

  private static String m(String message) {
    return "<b>Flex UI Designer</b><ul><li>" + message + "</li></ul>";
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
      root.refresh(false, true);
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
               !name.startsWith("FxComponentReferencedByClassName.") &&
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
}