// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.cli;

import com.google.gson.*;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.fixtures.BasePlatformTestCase;
import one.util.streamex.StreamEx;
import org.angular2.cli.config.AngularConfig;
import org.angular2.cli.config.AngularConfigProvider;
import org.angularjs.AngularTestUtil;
import org.jetbrains.annotations.NotNull;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

import java.io.File;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@RunWith(com.intellij.testFramework.Parameterized.class)
public class AngularConfigTest extends BasePlatformTestCase {

  @Parameterized.Parameter
  public String myDirName;

  @com.intellij.testFramework.Parameterized.Parameters(name = "{0}")
  public static List<String> testNames(@NotNull Class<?> klass) {
    File testData = new File(AngularTestUtil.getBaseTestDataPath(klass) + "config");
    return StreamEx.of(testData.listFiles())
      .filter(file -> file.isDirectory())
      .map(file -> file.getName())
      .toList();
  }

  @org.junit.runners.Parameterized.Parameters
  public static Collection<Object> data() {
    return new ArrayList<>();
  }

  @Override
  protected String getTestDataPath() {
    return AngularTestUtil.getBaseTestDataPath(AngularConfigTest.class) + "config";
  }

  @Test
  public void testParsing() {
    VirtualFile vFile = myFixture.copyDirectoryToProject(myDirName, "./");

    AngularConfig config = ReadAction.compute(() -> AngularConfigProvider.getAngularConfig(getProject(), vFile));

    assert config != null;

    myFixture.configureByText("out.txt", config.toString() + "\n");
    myFixture.checkResultByFile(myDirName + "/" + config.getAngularJsonFile().getName() + ".parsed",
                                true);
  }

  @Test
  public void testTsLintConfigSelection() throws Exception {
    VirtualFile rootDir = myFixture.copyDirectoryToProject(myDirName, "./");

    AngularConfig config = ReadAction.compute(() -> AngularConfigProvider.getAngularConfig(getProject(), rootDir));

    assert config != null;

    VirtualFile tslintTest = rootDir.findFileByRelativePath("tslint-test.json");

    assert tslintTest != null : "no tslint-test.json";

    JsonObject tests;
    try (InputStream in = tslintTest.getInputStream()) {
      tests = (JsonObject)new JsonParser().parse(new InputStreamReader(in, StandardCharsets.UTF_8));
    }

    for (Map.Entry<String, JsonElement> entry : tests.entrySet()) {
      VirtualFile file = myFixture.findFileInTempDir(entry.getKey());
      assert file != null : entry.getKey();
      String value = StreamEx.ofNullable(config.getProject(file))
        .flatCollection(project -> project.getTsLintConfigurations())
        .map(lintConfig -> lintConfig.getTsLintConfig(file))
        .nonNull()
        .findFirst()
        .map(lintFile -> lintFile.getPath())
        .orElse(null);
      if (value != null) {
        entry.setValue(new JsonPrimitive(value));
      }
      else {
        entry.setValue(null);
      }
    }
    myFixture.configureByText("out.txt",
                              new GsonBuilder().setPrettyPrinting()
                                .serializeNulls()
                                .create()
                                .toJson(tests) + "\n");
    myFixture.checkResultByFile(myDirName + "/tslint-test.json",
                                true);
  }
}
