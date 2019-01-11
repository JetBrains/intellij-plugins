// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.jetbrains.vuejs.service;

import com.intellij.lang.javascript.JSTestUtils;
import com.intellij.lang.javascript.service.JSLanguageService;
import com.intellij.lang.javascript.service.JSLanguageServiceBase;
import com.intellij.lang.javascript.service.JSLanguageServiceProvider;
import com.intellij.lang.javascript.typescript.service.TypeScriptServiceTestBase;
import com.intellij.lang.javascript.typescript.service.TypeScriptServiceTestRunner;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.vuejs.typescript.service.VueTypeScriptService;
import org.junit.runner.RunWith;

import java.util.List;

import static org.jetbrains.vuejs.language.VueTestUtilKt.getVueTestDataPath;

@RunWith(TypeScriptServiceTestRunner.class)
public class VueTypeScriptServiceTest extends TypeScriptServiceTestBase {
  private static final String BASE_PATH = "/ts_ls_highlighting";

  @Override
  protected String getTestDataPath() {
    return getVueTestDataPath();
  }

  @NotNull
  @Override
  protected JSLanguageServiceBase getService() {
    List<JSLanguageService> services = JSLanguageServiceProvider.getLanguageServices(myProject);

    return (JSLanguageServiceBase)ContainerUtil.find(services, el -> el instanceof VueTypeScriptService);
  }

  @Override
  protected String getExtension() {
    return "vue";
  }

  @Override
  protected String getBasePath() {
    return BASE_PATH;
  }


  public void testSimpleVue() throws Exception {
    doTestWithCopyDirectory();
  }

  @TypeScriptVersion(TypeScriptVersions.TS28)
  public void testSimpleCompletion() throws Exception {
    JSTestUtils.testES6(myProject, () -> checkBaseStringQualifiedCompletionWithTemplates(() -> doTestWithCopyDirectory()));
  }
}
