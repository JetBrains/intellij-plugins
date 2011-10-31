package com.intellij.flex.uiDesigner;

import com.intellij.openapi.roots.ContentEntry;
import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.StringBuilderSpinAllocator;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import static com.intellij.flex.uiDesigner.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Flex(version="4.5")
public class MxmlTest extends MxmlTestBase {
  @Override
  protected void modifyModule(ModifiableRootModel model, VirtualFile rootDir) {
    if (getName().equals("test45")) {
      addLibrary(model, getFudHome() + "/test-data-helper/target/test-data-helper.swc");
      final VirtualFile assetsDir = getVFile("assets");
      model.addContentEntry(assetsDir).addSourceFolder(assetsDir, false);

      final VirtualFile localesDir = getVFile("locales");
      final ContentEntry localesContentEntry = model.addContentEntry(localesDir);
      //noinspection ConstantConditions
      localesContentEntry.addSourceFolder(localesDir.findChild("en_US"), false);
      //localesContentEntry.addSourceFolder(localesDir.findChild("ru_RU"), false);
    }
    else if (getName().equals("testMobile")) {
      addLibrary(model, "mobilecomponents-4.5.1.swc");
    }
  }

  public void _testMobile() throws Exception {
    testFile("../mobile/SparkView.mxml");
  }

  public void test45() throws Exception {
    final String testFile = System.getProperty("testFile");
    final Pair<String[], String[]> pair = getTestFiles();
    final String[] files;
    if (testFile == null) {
      files = pair.getFirst();
    }
    else {
      files = new String[]{getTestPath() + "/" + testFile + ".mxml"};
    }

    final String[] auxFiles = pair.getSecond();
    final int auxFilesCount = auxFiles == null ? 0 : auxFiles.length;
    final VirtualFile[] vFiles = new VirtualFile[files.length + auxFilesCount];
    for (int i = 0; i < files.length; i++) {
      vFiles[i] = getVFile(files[i]);
    }

    if (auxFiles != null) {
      for (int i = files.length, j = 0; j < auxFiles.length;) {
        vFiles[i++] = getVFile(auxFiles[j++]);
      }
    }

    testFiles(vFiles.length - auxFilesCount, vFiles);
    
    String[] problems = getLastProblems();
    if (testFile != null) {
      assertThat(problems, emptyArray());
    }
    else {
      assertThat(problems,
                 m("Initializer for Group cannot be represented in text (line: 2)",
                   "Initializer for Container cannot be represented in text (line: 5)",
                   "Children of Accordion must be mx.core.INavigatorContent (line: 8)"),
                 m("Unresolved variable or type unresolvedData (line: 10)"),
                 m("Support only MXML-based component AuxActionScriptProjectComponent"),
                 m("<a href=\"http://youtrack.jetbrains.net/issue/IDEA-72175\">Inline components are not supported</a> (line: 9)"),
                 m("Invalid color name invalidcolorname (line: 2)", "Invalid integer value (line: 3)", "Invalid number value (line: 4)"),
                 m("Unsupported embed asset type \"@Embed(source='/jazz.mp3')\" (line: 3)"),
                 m("Invalid class value (line: 6)", "Invalid class value (line: 11)"),
                 m("Default property not found for Rect (line: 2)"),
                 m("spark.components.supportClasses.TrackBase is abstract class (line: 3)"));
    }
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

  private Pair<String[],String[]> getTestFiles() {
    ArrayList<String> files = new ArrayList<String>(64);
    ArrayList<String> auxFiles = new ArrayList<String>(8);
    
    collectMxmlFiles(files, auxFiles, new File(getTestPath()));
    collectMxmlFiles(files, auxFiles, new File(getTestDataPath() + "/src/mx"));

    String[] list = files.toArray(new String[files.size()]);
    String[] auxList = auxFiles.toArray(new String[auxFiles.size()]);
    
    Arrays.sort(list);
    Arrays.sort(auxList);
    
    return new Pair<String[], String[]>(list, auxList);
  }

  private static void collectMxmlFiles(ArrayList<String> files, ArrayList<String> auxFiles, File parent) {
    for (String name : parent.list()) {
      if (name.charAt(0) == '.') {
        // skip
      }
      else if (name.startsWith("Aux")) {
        auxFiles.add(parent.getPath() + "/" + name);
      }
      else if (name.endsWith(".mxml") && !name.startsWith("T.") && !name.startsWith("TestApp.") && !name.startsWith("GenericMxmlSupport.")) {
        files.add(parent.getPath() + "/" + name);
      }

      File file = new File(parent, name);
      if (file.isDirectory()) {
        collectMxmlFiles(files, auxFiles, file);
      }
    }
  }
}