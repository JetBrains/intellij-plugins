/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.intellij.protobuf.ide.settings;

import com.intellij.ide.impl.OpenProjectTask;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.protobuf.fixtures.PbCodeInsightTestUtil;
import com.intellij.protobuf.ide.settings.PbProjectSettings.ImportPathEntry;
import com.intellij.protobuf.lang.resolve.FileResolveProvider;
import com.intellij.protobuf.lang.resolve.FileResolveProvider.ChildEntry;
import com.intellij.testFramework.fixtures.CodeInsightTestFixture;
import com.intellij.testFramework.junit5.TestApplication;
import com.intellij.testFramework.junit5.fixture.TestFixture;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestInfo;

import java.io.File;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static com.google.common.truth.Truth.assertThat;
import static com.intellij.platform.testFramework.junit5.codeInsight.fixture.CodeInsightFixtureKt.codeInsightModuleFixture;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.projectFixture;
import static com.intellij.testFramework.junit5.fixture.FixturesKt.tempPathFixture;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

/** Unit tests for {@link SettingsFileResolveProvider}. */
@TestApplication
public class SettingsFileResolveProviderTest {
  @SuppressWarnings("deprecation")
  private static final TestFixture<Project> projectFixture = projectFixture(tempPathFixture(), OpenProjectTask.build(), true);

  private final TestFixture<CodeInsightTestFixture> codeInsightFixture = codeInsightModuleFixture(projectFixture);

  private CodeInsightTestFixture myFixture;

  private File tempDir = null;

  @BeforeEach
  void setUp(TestInfo testInfo) throws Exception {
    myFixture = codeInsightFixture.get();
    PbCodeInsightTestUtil.setUpTestDataPath(myFixture);
    tempDir = FileUtil.createTempDirectory(testInfo.getTestMethod().orElseThrow().getName(), UUID.randomUUID().toString(), false);
  }

  @AfterEach
  void tearDown() {
    try {
      // Reset settings state
      PbProjectSettings.getInstance(myFixture.getProject()).loadState(new PbProjectSettings.State());
    }
    finally {
      FileUtil.delete(tempDir);
    }
  }

  @Test
  public void testFindFileWithNoSettingsReturnsNull() {
    assertNull(new SettingsFileResolveProvider().findFile("missing/file", myFixture.getProject()));
  }

  @Test
  public void testFindFilePrefersFirstListedPath() throws Exception {
    FileUtil.writeToFile(new File(tempDir, "path1/dir/foo.proto"), "// foo in path1");
    FileUtil.writeToFile(new File(tempDir, "path2/dir/foo.proto"), "// foo in path2");
    FileUtil.writeToFile(new File(tempDir, "path2/dir/bar.proto"), "// bar in path2");

    PbProjectSettings settings = PbProjectSettings.getInstance(myFixture.getProject());
    settings.setImportPathEntries(
        Arrays.asList(
            new ImportPathEntry(VfsUtil.pathToUrl(new File(tempDir, "path1").getPath()), "com/foo"),
            new ImportPathEntry(
                VfsUtil.pathToUrl(new File(tempDir, "path2").getPath()), "com/foo")));

    FileResolveProvider resolver = new SettingsFileResolveProvider();
    VirtualFile foo = resolver.findFile("com/foo/dir/foo.proto", myFixture.getProject());
    VirtualFile bar = resolver.findFile("com/foo/dir/bar.proto", myFixture.getProject());

    assertNotNull(foo);
    assertNotNull(bar);

    assertEquals("// foo in path1", VfsUtil.loadText(foo));
    assertEquals("// bar in path2", VfsUtil.loadText(bar));
  }

  @Test
  public void testGetChildEntries() throws Exception {
    FileUtil.writeToFile(new File(tempDir, "path1/dir/foo.proto"), "// foo in path1");
    FileUtil.writeToFile(new File(tempDir, "path2/dir/foo.proto"), "// foo in path2");
    FileUtil.writeToFile(new File(tempDir, "path2/dir/bar.proto"), "// bar in path2");
    FileUtil.writeToFile(new File(tempDir, "path3/dir/bar.proto"), "// bar in path3");

    PbProjectSettings settings = PbProjectSettings.getInstance(myFixture.getProject());
    settings.setImportPathEntries(
        Arrays.asList(
            new ImportPathEntry(VfsUtil.pathToUrl(new File(tempDir, "path1").getPath()), "com/foo"),
            new ImportPathEntry(VfsUtil.pathToUrl(new File(tempDir, "path2").getPath()), "com/foo"),
            new ImportPathEntry(VfsUtil.pathToUrl(new File(tempDir, "path3").getPath()), "")));

    FileResolveProvider resolver = new SettingsFileResolveProvider();
    assertTrue(resolver.getChildEntries("", myFixture.getProject()).containsAll(List.of(ChildEntry.directory("dir"), ChildEntry.directory("com"))));
    assertThat(resolver.getChildEntries("dir", myFixture.getProject())).contains(ChildEntry.file("bar.proto"));
    assertThat(resolver.getChildEntries("com", myFixture.getProject())).contains(ChildEntry.directory("foo"));
    assertThat(resolver.getChildEntries("com/", myFixture.getProject())).contains(ChildEntry.directory("foo"));
    assertThat(resolver.getChildEntries("com/foo", myFixture.getProject())).contains(ChildEntry.directory("dir"));
    assertTrue(resolver.getChildEntries("com/foo/dir", myFixture.getProject()).containsAll(List.of(ChildEntry.file("foo.proto"), ChildEntry.file("bar.proto"))));
    assertTrue(resolver.getChildEntries("com/foo/dir/", myFixture.getProject()).containsAll(List.of(ChildEntry.file("foo.proto"), ChildEntry.file("bar.proto"))));
  }
}
