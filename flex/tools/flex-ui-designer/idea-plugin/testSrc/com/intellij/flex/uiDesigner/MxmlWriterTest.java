package com.intellij.flex.uiDesigner;

import com.intellij.openapi.vfs.LocalFileSystem;
import com.intellij.openapi.vfs.VirtualFile;
import js.JSTestOptions;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;

import static js.JSTestOption.WithFlexSdk;
import static js.JSTestOption.WithGumboSdk;

public class MxmlWriterTest extends MxmlWriterTestBase {
  @JSTestOptions({WithGumboSdk, WithFlexSdk})
  @Flex(version="4.5")
  public void test45() throws Exception {
    String testFile = System.getProperty("testFile");
    String[] files = testFile == null ? getTestFiles() : new String[]{getTestPath() + "/" + testFile + ".mxml"};

    final VirtualFile[] vFiles = new VirtualFile[files.length + 1];
    for (int i = 0; i < files.length; i++) {
      VirtualFile vFile = LocalFileSystem.getInstance().findFileByPath(files[i]);
      assert vFile != null;
      vFiles[i] = vFile;
    }
    
    vFiles[files.length] = LocalFileSystem.getInstance().findFileByPath(getTestPath() + "/background.jpg");
    assert vFiles[files.length] != null;

    testFiles(vFiles);
  }

  @JSTestOptions({WithGumboSdk, WithFlexSdk})
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
      else if (name.endsWith(".mxml")) {
        files.add(parent.getPath() + "/" + name);
      }
      File file = new File(parent, name);
      if (file.isDirectory()) {
        collectMxmlFiles(files, file);
      }
    }
  }
}
