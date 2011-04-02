package com.intellij.flex.uiDesigner;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.vfs.VirtualFile;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

@Flex(version="4.5")
public class MxmlWriterTest extends MxmlWriterTestBase {
  public void test45() throws Exception {
    changeServiceImplementation(FlexUIDesignerApplicationManager.class, MyFlexUIDesignerApplicationManager.class);
    
    String testFile = System.getProperty("testFile");
    String[] files = testFile == null ? getTestFiles() : new String[]{getTestPath() + "/" + testFile + ".mxml"};

    final VirtualFile[] vFiles = new VirtualFile[files.length + 3];
    for (int i = 0; i < files.length; i++) {
      vFiles[i] = getVFile(files[i]);
    }
    
    vFiles[files.length] = getVFile(getTestPath() + "/background.jpg");
    vFiles[files.length + 1] = getVFile(getTestPath() + "/background.p-n-g");
    vFiles[files.length + 2] = getVFile(getTestPath() + "/anim.swf");

    testFiles(vFiles);
    
    final List<String> problems = ((MyFlexUIDesignerApplicationManager)FlexUIDesignerApplicationManager.getInstance()).problems;
    if (testFile != null) {
      assertEquals(0, problems.size());
    }
    else {
      assertEquals(1, problems.size());
      assertEquals("Invalid color name: invalidcolorname", problems.get(0));
    }
  }

  @Flex(version="4.1")
  public void test41() throws Exception {
    testFile("states/UnusedStates.mxml");
  }

  private String[] getTestFiles() {
    ArrayList<String> files = new ArrayList<String>(20);
    collectMxmlFiles(files, new File(getTestPath()));
    String[] list = files.toArray(new String[files.size()]);
    Arrays.sort(list);
    return list;
  }

  private void collectMxmlFiles(ArrayList<String> files, File parent) {
    for (String name : parent.list()) {
      if (name.charAt(0) == '.') {
        // skip
      }
      else if (name.endsWith(".mxml") && !name.startsWith("TestApp.")) {
        files.add(parent.getPath() + "/" + name);
      }
      File file = new File(parent, name);
      if (file.isDirectory()) {
        collectMxmlFiles(files, file);
      }
    }
  }

  private static class MyFlexUIDesignerApplicationManager extends FlexUIDesignerApplicationManager {
    private final List<String> problems = new ArrayList<String>();
    
    @Override
    public void reportProblem(Project project, String message, MessageType messageType) {
      problems.add(message);
    }
  }
}
