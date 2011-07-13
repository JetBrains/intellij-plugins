package com.intellij.flex.uiDesigner;

import com.intellij.openapi.roots.ModifiableRootModel;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import static com.intellij.flex.uiDesigner.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.*;

@Flex(version="4.5")
public class MxmlTest extends MxmlWriterTestBase {
  @Override
  protected void modifyModule(ModifiableRootModel model, VirtualFile rootDir) {
    if (getName().equals("test45")) {
      addLibrary(model, getFudHome() + "/test-data-libs/target/test-data-libs.swc");
      final VirtualFile assetsDir = getVFile(getTestDataPath() + "/assets");
      model.addContentEntry(assetsDir).addSourceFolder(assetsDir, false);
    }
  }

  public void test45() throws Exception {
    String testFile = System.getProperty("testFile");
    String[] files = testFile == null ? getTestFiles() : new String[]{getTestPath() + "/" + testFile + ".mxml"};

    final VirtualFile[] vFiles = new VirtualFile[files.length + 1];
    for (int i = 0; i < files.length; i++) {
      vFiles[i] = getVFile(files[i]);
    }
    
    vFiles[files.length] = getVFile(getTestPath() + "/anim.swf");

    testFiles(vFiles);
    
    String[] problems = getLastProblems();
    if (testFile != null) {
      assertThat(problems, emptyArray());
    }
    else {
      assertThat(problems,
                 m("Unresolved variable unresolvedData"),
                 m("Invalid color name invalidcolorname"),
                 m("Default property not found for Rect (line: 2)"),
                 m("spark.components.supportClasses.TrackBase is abstract class"));
    }
  }

  private static String m(String message) {
    return "<html><b>Flex UI Designer</b><ul><li>" + message + "</li></ul></html>";
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

  private String[] getTestFiles() {
    ArrayList<String> files = new ArrayList<String>(20);
    collectMxmlFiles(files, new File(getTestPath()));
    String[] list = files.toArray(new String[files.size()]);
    Arrays.sort(list);
    return list;
  }

  private static void collectMxmlFiles(ArrayList<String> files, File parent) {
    for (String name : parent.list()) {
      if (name.charAt(0) == '.') {
        // skip
      }
      else if (name.endsWith(".mxml") && !name.startsWith("T.") && !name.startsWith("TestApp.") && !name.startsWith("Constructor.")) {
        files.add(parent.getPath() + "/" + name);
      }
      File file = new File(parent, name);
      if (file.isDirectory() && !name.equals("mobile")) {
        collectMxmlFiles(files, file);
      }
    }
  }
}