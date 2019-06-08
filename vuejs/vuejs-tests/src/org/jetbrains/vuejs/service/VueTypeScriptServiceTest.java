// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.service;

import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.lang.javascript.service.JSLanguageService;
import com.intellij.lang.javascript.service.JSLanguageServiceBase;
import com.intellij.lang.javascript.service.JSLanguageServiceProvider;
import com.intellij.lang.javascript.typescript.service.TypeScriptServiceTestBase;
import com.intellij.lang.javascript.typescript.service.TypeScriptServiceTestRunner;
import com.intellij.openapi.fileEditor.FileDocumentManager;
import com.intellij.util.containers.ContainerUtil;
import com.intellij.util.ui.UIUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.vuejs.typescript.service.VueTypeScriptService;
import org.junit.runner.RunWith;

import java.util.List;

import static org.jetbrains.vuejs.language.VueTestUtilKt.vueRelativeTestDataPath;

@RunWith(TypeScriptServiceTestRunner.class)
public class VueTypeScriptServiceTest extends TypeScriptServiceTestBase {
  private static final String BASE_PATH = "/ts_ls_highlighting";


  @NotNull
  @Override
  protected JSLanguageServiceBase getService() {
    List<JSLanguageService> services = JSLanguageServiceProvider.getLanguageServices(getProject());

    return (JSLanguageServiceBase)ContainerUtil.find(services, el -> el instanceof VueTypeScriptService);
  }

  @NotNull
  @Override
  protected String getExtension() {
    return "vue";
  }

  private void completeTsLangAndAssert() {
    doTestWithCopyDirectory();
    myFixture.type(" lang=\"\bts\"");

    FileDocumentManager.getInstance().saveDocument(myFixture.getDocument(myFixture.getFile()));

    UIUtil.dispatchAllInvocationEvents();
    checkAfterFile("vue");
  }

  @Override
  protected String getBasePath() {
    return vueRelativeTestDataPath() + BASE_PATH;
  }


  @TypeScriptVersion(TypeScriptVersions.TS28)
  public void testSimpleVue() {
    doTestWithCopyDirectory();

    myFixture.configureByFile("SimpleVueNoTs.vue");
    checkHighlightingByOptions(false);
  }

  @TypeScriptVersion(TypeScriptVersions.TS28)
  public void testSimpleCompletion() throws Exception {
    checkBaseStringQualifiedCompletionWithTemplates(() -> {
      doTestWithCopyDirectory();
      return myFixture.complete(CompletionType.BASIC);
    });
  }

  @TypeScriptVersion(TypeScriptVersions.TS28)
  public void testSimpleVueNoTs() {
    doTestWithCopyDirectory();

    myFixture.configureByFile("SimpleVue.vue");
    checkHighlightingByOptions(false);
  }

  @TypeScriptVersion(TypeScriptVersions.TS28)
  public void testSimpleVueEditing() {
    doTestWithCopyDirectory();
    myFixture.type('\b');

    checkAfterFile("vue");
    myFixture.type('s');
    checkAfterFile("2.vue");
  }

  @TypeScriptVersion(TypeScriptVersions.TS28)
  public void testSimpleVueEditingNoTs() {
    completeTsLangAndAssert();
  }

  @TypeScriptVersion(TypeScriptVersions.TS28)
  public void testSimpleVueEditingNoTsNoRefs() {
    completeTsLangAndAssert();
  }

  @TypeScriptVersion(TypeScriptVersions.TS28)
  public void testSimpleVueEditingCloseTag() {
    doTestWithCopyDirectory();
    myFixture.type('\b');
    checkAfterFile("vue");
    myFixture.type('/');
    checkAfterFile("2.vue");
  }
}
