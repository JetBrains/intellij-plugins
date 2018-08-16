// Copyright 2000-2018 JetBrains s.r.o.
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
// http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.
package com.intellij.javascript.karma.server;

import com.intellij.openapi.vfs.VfsUtilCore;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.testFramework.fixtures.CodeInsightFixtureTestCase;
import org.jetbrains.annotations.NotNull;
import org.junit.Assert;

public class AngularCliConfigTest extends CodeInsightFixtureTestCase {

  public static String getTestDataBasePath(@NotNull Class testClass) {
    return "/contrib/js-karma/test/" + testClass.getPackage().getName().replace('.', '/')
           + "/data_" + testClass.getSimpleName();
  }

  @Override
  protected String getBasePath() {
    return getTestDataBasePath(AngularCliConfigTest.class);
  }

  public void testMalformedConfigWithMultipleIdenticalRoots() {
    VirtualFile dir = myFixture.copyDirectoryToProject(getTestName(true), getTestName(true));
    AngularCliConfig config = AngularCliConfig.findProjectConfig(VfsUtilCore.virtualToIoFile(dir));
    Assert.assertNotNull(config);

    VirtualFile karmaConfig = dir.findFileByRelativePath("src/karma.conf.js");
    Assert.assertNotNull(karmaConfig);
    Assert.assertEquals("sample", config.getProjectContainingFileOrDefault(karmaConfig));
  }

  public void testMultiProject() {
    VirtualFile dir = myFixture.copyDirectoryToProject("multiProject", "multiProject");
    AngularCliConfig config = AngularCliConfig.findProjectConfig(VfsUtilCore.virtualToIoFile(dir));
    Assert.assertNotNull(config);

    VirtualFile karmaConfig1 = dir.findFileByRelativePath("src/karma.conf.js");
    Assert.assertNotNull(karmaConfig1);
    Assert.assertEquals("untitled5", config.getProjectContainingFileOrDefault(karmaConfig1));

    VirtualFile karmaConfig2 = dir.findFileByRelativePath("projects/app-two/karma.conf.js");
    Assert.assertNotNull(karmaConfig2);
    Assert.assertEquals("app-two", config.getProjectContainingFileOrDefault(karmaConfig2));
  }
}
