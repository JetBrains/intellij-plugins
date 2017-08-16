package org.angularjs.service;


import com.intellij.lang.typescript.compiler.TypeScriptCompilerSettings;
import com.intellij.lang.typescript.compiler.languageService.TypeScriptLanguageServiceUtil;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import org.angularjs.AngularTestUtil;

import java.io.File;

public class Angular2ServiceTest extends CodeInsightFixtureTestCase {

  private volatile boolean skipTests = false;

  @Override
  protected String getBasePath() {
    return AngularTestUtil.getBaseTestDataPath(getClass());
  }


  @Override
  protected void setUp() throws Exception {
    super.setUp();

    myFixture.setTestDataPath(getBasePath());
    final File fromFile = new File(getBasePath() + "/defaultData/node_modules");
    if (!fromFile.exists()) {
      skipTests = true;
      return;
    }

    enableService(myFixture.getProject(), myFixture.getTestRootDisposable());


    myFixture.copyDirectoryToProject("/defaultData", "");
    myFixture.copyDirectoryToProject("/" + getTestName(false), "");
  }


  public void enableService(Project project, Disposable disposable) {
    Disposer.register(disposable, () -> TypeScriptLanguageServiceUtil.setUseService(false));
    TypeScriptLanguageServiceUtil.setUseService(true);
    registerRestoreForSettings(TypeScriptCompilerSettings.getSettings(project), disposable);
  }

  protected void registerRestoreForSettings(TypeScriptCompilerSettings settings, Disposable disposable) {
    boolean compilerEnabled = settings.isCompilerEnabled();
    boolean changes = settings.isTrackFileSystemChanges();
    boolean isMainFile = settings.isUseMainFile();
    boolean service = settings.isUseService();
    String mainFilePath = settings.getMainFilePath();
    String outDir = settings.getOutDirectory();
    boolean hasOutDirectory = settings.isHasOutDirectory();
    String params = settings.getTypeScriptCompilerParams();
    boolean useConfig = settings.isUseConfigForCompiler();
    boolean showAllProjectErrors = settings.isShowAllProjectErrors();

    Disposer.register(disposable, () -> {
      settings.setUseService(service);
      settings.setImmediateCompileEnabled(compilerEnabled);
      settings.setTrackFileSystemChanges(changes);
      settings.setMainFilePath(mainFilePath);
      settings.setUseMainFile(isMainFile);
      settings.setHasOutDirectory(hasOutDirectory);
      settings.setOutDirectory(outDir);
      settings.setTypeScriptCompilerParams(params);
      settings.setUseConfigForCompiler(useConfig);
      settings.setShowAllProjectErrors(showAllProjectErrors);
    });
  }


  private void defaultTest(String extension) {
    if (skipTests) {
      return;
    }

    doTestFor(getTestName(false) + extension);
  }


  protected void doTestFor(String file) {
    myFixture.configureFromTempProjectFile(file);

    if (skipTests) {
      return;
    }

    myFixture.checkHighlighting();
  }

  public void testSimpleHighlight() {
    defaultTest(".ts");
  }

  public void testSimpleHighlightHtml() {
    defaultTest(".html");
  }
}
