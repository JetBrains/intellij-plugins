package com.jetbrains.lang.dart.analyzer;

import com.google.dart.compiler.*;
import com.google.dart.compiler.ast.DartUnit;
import com.intellij.codeInsight.daemon.impl.AnnotationHolderImpl;
import com.intellij.lang.annotation.Annotation;
import com.intellij.lang.annotation.AnnotationSession;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.openapi.vfs.VirtualFileManager;
import com.intellij.testFramework.fixtures.JavaCodeInsightFixtureTestCase;
import com.intellij.util.ArrayUtil;
import com.intellij.util.containers.ContainerUtil;
import com.jetbrains.lang.dart.ide.annotator.DartExternalAnnotator;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.jetbrains.annotations.Nullable;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

abstract public class DartAnalyzerTestBase extends JavaCodeInsightFixtureTestCase {

  @Override
  public void setUp() throws Exception {
    super.setUp();
    System.setProperty(
      "com.google.dart.sdk",
      PathManager.getHomePath() + FileUtil.toSystemDependentName("/plugins/Dart/testData/sdk")
    );
  }

  void doTest(String message, String... additionalFiles) throws IOException {
    final String fullTestName = getTestName(true);
    final int dollarIndex = fullTestName.lastIndexOf('$');
    final String fixSimpleClassName = dollarIndex > 0 ? fullTestName.substring(dollarIndex + 1) : null;
    final String testName = dollarIndex > 0 ? fullTestName.substring(0, dollarIndex) : fullTestName;

    String[] files = ArrayUtil.append(additionalFiles, testName + ".dart");
    files = ArrayUtil.reverseArray(files);

    myFixture.configureByFiles(files);

    final Annotation annotation = doHighlightingAndFindIntention(message);
    assertNotNull("Can't find intention", annotation);

    final List<Annotation.QuickFixInfo> quickFixes = annotation.getQuickFixes();
    assertNotNull("Can't find fixes", quickFixes);
    assertFalse(quickFixes.isEmpty());

    final Annotation.QuickFixInfo quickFixInfo = ContainerUtil.find(quickFixes, new Condition<Annotation.QuickFixInfo>() {
      @Override
      public boolean value(Annotation.QuickFixInfo info) {
        return fixSimpleClassName == null || info.quickFix.getClass().getSimpleName().equals(fixSimpleClassName);
      }
    });
    assertNotNull("Can't find fixes", quickFixInfo);
    assertTrue("Fix not available", quickFixInfo.quickFix.isAvailable(myFixture.getProject(), myFixture.getEditor(), myFixture.getFile()));

    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      @Override
      public void run() {
        quickFixInfo.quickFix.invoke(myFixture.getProject(), myFixture.getEditor(), myFixture.getFile());
      }
    });
    myFixture.checkResultByFile(fullTestName + ".after.dart");
  }

  @Nullable
  private Annotation doHighlightingAndFindIntention(final String message) throws IOException {
    final AnnotationHolderImpl annotationHolder = new AnnotationHolderImpl(new AnnotationSession(myFixture.getFile()));

    new DartExternalAnnotator().apply(myFixture.getFile(), getMessagesFromAnalyzer(), annotationHolder);

    return ContainerUtil.find(annotationHolder, new Condition<Annotation>() {
      @Override
      public boolean value(Annotation action) {
        return message.equals(action.getMessage());
      }
    });
  }

  private List<AnalyzerMessage> getMessagesFromAnalyzer() throws IOException {
    final List<AnalyzerMessage> result = new ArrayList<AnalyzerMessage>();

    DefaultDartArtifactProvider provider = new DefaultDartArtifactProvider() {
      @Override
      public Writer getArtifactWriter(Source source, String part, String extension) throws IOException {
        return new Writer() {
          @Override
          public void write(char[] cbuf, int off, int len) throws IOException {
          }

          @Override
          public void flush() throws IOException {
          }

          @Override
          public void close() throws IOException {
          }
        };
      }
    };
    DefaultCompilerConfiguration config = new DefaultCompilerConfiguration();
    File libFile = new File(DartResolveUtil.getRealVirtualFile(myFixture.getFile()).getPath());
    final LibrarySource lib = new UrlLibrarySource(libFile);
    DartCompiler.compileLib(lib, config, provider, new DartCompilerListener() {
      @Override
      public void onError(DartCompilationError event) {
        String url = VfsUtilCore.pathToUrl(event.getSource().getUri().getPath());
        VirtualFile fileByUrl = VirtualFileManager.getInstance().findFileByUrl(url);
        if (fileByUrl == null) {
          return;
        }
        AnalyzerMessage message = new AnalyzerMessage(
          fileByUrl,
          event.getLineNumber() - 1,
          event.getColumnNumber() - 1,
          event.getLength(),
          AnalyzerMessage.Type.WARNING,
          event.getErrorCode().getSubSystem().toString(),
          event.getErrorCode().toString(),
          event.getMessage()
        );
        result.add(message);
      }

      @Override
      public void unitAboutToCompile(DartSource source, boolean diet) {
      }

      @Override
      public void unitCompiled(DartUnit unit) {
      }
    });

    return result;
  }
}
