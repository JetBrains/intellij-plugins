package com.google.jstestdriver.idea.assertFramework;

import com.google.jstestdriver.idea.assertFramework.jasmine.JasmineFileStructure;
import com.google.jstestdriver.idea.assertFramework.jasmine.JasmineFileStructureBuilder;
import com.google.jstestdriver.idea.assertFramework.jasmine.JasmineSuiteStructure;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.util.ObjectUtils;
import com.intellij.util.containers.HashMap;
import com.intellij.util.indexing.*;
import com.intellij.util.io.DataExternalizer;
import com.intellij.util.io.EnumeratorStringDescriptor;
import com.intellij.util.io.KeyDescriptor;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author Sergey Simonchik
 */
public class JsTestFileByTestNameIndex extends FileBasedIndexExtension<String, Void> {

  private static final ID<String, Void> KEY = ID.create("js.test.names");

  private final KeyDescriptor<String> myKeyDescriptor = new EnumeratorStringDescriptor();

  private static final FileBasedIndex.InputFilter JS_FILE_INPUT_FILTER = new FileBasedIndex.InputFilter() {
    @Override
    public boolean acceptInput(final VirtualFile file) {
      return JavaScriptSupportLoader.JAVASCRIPT == file.getFileType();
    }
  };

  @NotNull
  @Override
  public ID<String, Void> getName() {
    return KEY;
  }

  @NotNull
  @Override
  public DataIndexer<String, Void, FileContent> getIndexer() {
    return new DataIndexer<String, Void, FileContent>() {
      @NotNull
      @Override
      public Map<String, Void> map(FileContent inputData) {
        JSFile jsFile = ObjectUtils.tryCast(inputData.getPsiFile(), JSFile.class);
        if (jsFile == null) {
          return Collections.emptyMap();
        }
        JasmineFileStructureBuilder jasmineFileStructureBuilder = JasmineFileStructureBuilder.getInstance();
        JasmineFileStructure jasmineFileStructure = jasmineFileStructureBuilder.fetchCachedTestFileStructure(jsFile);
        if (!jasmineFileStructure.hasJasmineSymbols()) {
          return Collections.emptyMap();
        }
        Map<String, Void> testNames = new HashMap<String, Void>();
        for (JasmineSuiteStructure suiteStructure : jasmineFileStructure.getSuites()) {
          addAllDescendantSuites(testNames, suiteStructure, "");
        }
        return testNames;
      }
    };
  }

  private void addAllDescendantSuites(@NotNull Map<String, Void> testNames, @NotNull JasmineSuiteStructure suite,
                                      @NotNull String prefix) {
    String suiteName = prefix.isEmpty() ? suite.getName() : prefix + " " + suite.getName();
    testNames.put(suiteName, null);
    for (JasmineSuiteStructure childSuite : suite.getSuites()) {
      addAllDescendantSuites(testNames, childSuite, suiteName);
    }
  }

  @Override
  public KeyDescriptor<String> getKeyDescriptor() {
    return myKeyDescriptor;
  }

  @Override
  public DataExternalizer<Void> getValueExternalizer() {
    return ScalarIndexExtension.VOID_DATA_EXTERNALIZER;
  }

  @Override
  public FileBasedIndex.InputFilter getInputFilter() {
    return JS_FILE_INPUT_FILTER;
  }

  @Override
  public boolean dependsOnFileContent() {
    return true;
  }

  @Override
  public int getVersion() {
    return 1;
  }

  @NotNull
  public static List<VirtualFile> findJsTestFilesByNameInScope(@NotNull String testCaseName, @NotNull GlobalSearchScope scope) {
    final List<VirtualFile> jsTestFileList = new ArrayList<VirtualFile>(1);
    FileBasedIndex.getInstance().processValues(
      KEY,
      testCaseName,
      null,
      new FileBasedIndex.ValueProcessor<Void>() {
        @Override
        public boolean process(final VirtualFile file, final Void value) {
          if (file != null) {
            jsTestFileList.add(file);
          }
          return true;
        }
      },
      scope
    );
    return jsTestFileList;
  }

}
