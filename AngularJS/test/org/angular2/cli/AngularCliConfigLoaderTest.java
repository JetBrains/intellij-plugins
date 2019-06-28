// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.cli;

import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.EdtTestUtil;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import one.util.streamex.StreamEx;
import org.angularjs.AngularTestUtil;
import org.apache.commons.lang.SystemUtils;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.jetbrains.annotations.NotNull;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

@RunWith(com.intellij.testFramework.Parameterized.class)
public class AngularCliConfigLoaderTest extends BasePlatformTestCase {

  @Parameterized.Parameter
  public String myFileName;

  @com.intellij.testFramework.Parameterized.Parameters(name = "{0}")
  public static List<String> testNames(@NotNull Class<?> klass) {
    File testData = new File(AngularTestUtil.getBaseTestDataPath(klass) + "config");
    return StreamEx.of(testData.list())
      .filter(name -> name.endsWith(".json"))
      .map(name -> name.substring(0, name.length() - 5))
      .toList();
  }

  @org.junit.runners.Parameterized.Parameters
  public static Collection<Object> data() {
    return new ArrayList<>();
  }

  @Override
  @Before
  public void setUp() throws Exception {
    super.setUp();
  }

  @Override
  @After
  public void tearDown() {
    EdtTestUtil.runInEdtAndWait(() -> super.tearDown());
  }

  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(AngularCliConfigLoaderTest.class) + "config";
  }

  @Test
  public void doSingleTest() {
    VirtualFile vFile = myFixture.copyFileToProject(
      myFileName + ".json", "angular.json");

    AngularCliConfig config = ReadAction.compute(() -> AngularCliConfigLoader.load(getProject(), vFile));

    myFixture.configureByText("out.txt", ToStringBuilder.reflectionToString(config, new ToStringStyle() {{
      this.setUseShortClassName(true);
      this.setUseIdentityHashCode(false);

      this.setContentStart("[");
      this.setFieldSeparator(SystemUtils.LINE_SEPARATOR + "  ");
      this.setFieldSeparatorAtStart(true);
      this.setContentEnd(SystemUtils.LINE_SEPARATOR + "]");
    }})+ "\n");
    myFixture.checkResultByFile(myFileName + ".parsed.txt", true);
  }
}
