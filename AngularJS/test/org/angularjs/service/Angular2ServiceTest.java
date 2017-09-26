package org.angularjs.service;


import com.intellij.lang.javascript.TypeScriptTestUtil;
import com.intellij.lang.javascript.integration.JSAnnotationError;
import com.intellij.lang.javascript.service.JSLanguageService;
import com.intellij.lang.javascript.service.JSLanguageServiceProvider;
import com.intellij.lang.typescript.compiler.TypeScriptCompilerSettings;
import com.intellij.lang.typescript.compiler.languageService.TypeScriptLanguageServiceUtil;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.Ref;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import org.angularjs.AngularTestUtil;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.util.List;

import static com.intellij.lang.javascript.TypeScriptTestUtil.waitEmptyServiceQueueForService;

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
    boolean useForProjectsWithoutConfig = settings.useServiceForProjectsWithoutConfig();
    boolean changes = settings.isRecompileOnChanges();
    boolean isMainFile = settings.isUseMainFile();
    boolean useDeprecatedSettings = settings.useDeprecatedSettings();
    boolean service = settings.useService();
    String mainFilePath = settings.getMainFilePath();
    String outDir = settings.getOutDirectory();
    boolean hasOutDirectory = settings.isHasOutDirectory();
    String params = settings.getDefaultServiceOptions();
    boolean showAllProjectErrors = settings.isShowAllProjectErrors();

    Disposer.register(disposable, () -> {
      settings.setUseService(service);
      settings.setUseServiceForProjectsWithoutConfig(useForProjectsWithoutConfig);
      settings.setRecompileOnChanges(changes);
      settings.setMainFilePath(mainFilePath);
      settings.setUseMainFile(isMainFile);
      settings.setHasOutDirectory(hasOutDirectory);
      settings.setOutDirectory(outDir);
      settings.setDefaultServiceOptions(params);
      settings.setUseDeprecatedSettings(useDeprecatedSettings);
      settings.setShowAllProjectErrors(showAllProjectErrors);
    });
  }


  private void defaultTest(String extension) {
    doTestFor(getTestName(false) + extension);
  }


  protected void doTestFor(String file) {
    myFixture.configureFromTempProjectFile(file);
    myFixture.checkHighlighting();
  }

  public void testSimpleHighlight() {
    if (skipTests) {
      return;
    }
    Ref<List<JSAnnotationError>> reporter = getReporter(true);
    defaultTest(".ts");

    waitEmptyServiceQueueForService(getAngularLS());

    List<JSAnnotationError> errors = reporter.get();
    assertNotEmpty(errors);
    assertSize(2, errors);
  }

  public void testSimpleHighlightHtml() {
    if (skipTests) {
      return;
    }
    Ref<List<JSAnnotationError>> reporter = getReporter(true);
    defaultTest(".html");

    waitEmptyServiceQueueForService(getAngularLS());

    List<JSAnnotationError> errors = reporter.get();
    assertNotEmpty(errors);
    assertSize(2, errors);
  }
  
  
  public void testSimpleHighlightHtmlNotAngular() {
    if (skipTests) {
      return;
    }
    defaultTest(".html");
  }

  public void testSimpleHighlightHtmlNotAngularUnderConfig() {
    if (skipTests) {
      return;
    }
    defaultTest(".html");
  }

  @NotNull
  public Ref<List<JSAnnotationError>> getReporter(boolean projectErrors) {
    Angular2LanguageService angularLS = getAngularLS();

    return TypeScriptTestUtil.getServiceErrorReporter(getProject(), angularLS, projectErrors);
  }

  @NotNull
  private Angular2LanguageService getAngularLS() {
    List<JSLanguageService> services = JSLanguageServiceProvider.getLanguageServices(getProject());

    for (JSLanguageService service : services) {
      if (service instanceof Angular2LanguageService) {
        return (Angular2LanguageService)service;
      }
    }

    assert false : "Cannot find angular ls";
    return null;
  }
}
