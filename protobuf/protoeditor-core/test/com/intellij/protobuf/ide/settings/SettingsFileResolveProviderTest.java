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

import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VfsUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.protobuf.fixtures.PbCodeInsightFixtureTestCase;
import com.intellij.protobuf.ide.settings.PbProjectSettings.ImportPathEntry;
import com.intellij.protobuf.lang.resolve.FileResolveProvider;
import com.intellij.protobuf.lang.resolve.FileResolveProvider.ChildEntry;

import java.io.File;
import java.util.Arrays;
import java.util.UUID;

import static com.intellij.protobuf.TestUtils.notNull;

/** Unit tests for {@link SettingsFileResolveProvider}. */
public class SettingsFileResolveProviderTest extends PbCodeInsightFixtureTestCase {

  private File tempDir = null;

  @Override
  public void setUp() throws Exception {
    super.setUp();
    tempDir = FileUtil.createTempDirectory(getName(), UUID.randomUUID().toString(), false);
  }

  @Override
  public void tearDown() throws Exception {
    // Reset settings state
    PbProjectSettings.getInstance(getProject()).loadState(new PbProjectSettings.State());
    FileUtil.delete(tempDir);
    super.tearDown();
  }

  public void testFindFileWithNoSettingsReturnsNull() {
    assertNull(new SettingsFileResolveProvider().findFile("missing/file", getProject()));
  }

  public void testFindFilePrefersFirstListedPath() throws Exception {
    FileUtil.writeToFile(new File(tempDir, "path1/dir/foo.proto"), "// foo in path1");
    FileUtil.writeToFile(new File(tempDir, "path2/dir/foo.proto"), "// foo in path2");
    FileUtil.writeToFile(new File(tempDir, "path2/dir/bar.proto"), "// bar in path2");

    PbProjectSettings settings = PbProjectSettings.getInstance(getProject());
    settings.setImportPathEntries(
        Arrays.asList(
            new ImportPathEntry(VfsUtil.pathToUrl(new File(tempDir, "path1").getPath()), "com/foo"),
            new ImportPathEntry(
                VfsUtil.pathToUrl(new File(tempDir, "path2").getPath()), "com/foo")));

    FileResolveProvider resolver = new SettingsFileResolveProvider();
    VirtualFile foo = resolver.findFile("com/foo/dir/foo.proto", getProject());
    VirtualFile bar = resolver.findFile("com/foo/dir/bar.proto", getProject());

    assertNotNull(foo);
    assertNotNull(bar);

    assertEquals("// foo in path1", VfsUtil.loadText(foo));
    assertEquals("// bar in path2", VfsUtil.loadText(bar));
  }

  public void testGetChildEntries() throws Exception {
    FileUtil.writeToFile(new File(tempDir, "path1/dir/foo.proto"), "// foo in path1");
    FileUtil.writeToFile(new File(tempDir, "path2/dir/foo.proto"), "// foo in path2");
    FileUtil.writeToFile(new File(tempDir, "path2/dir/bar.proto"), "// bar in path2");
    FileUtil.writeToFile(new File(tempDir, "path3/dir/bar.proto"), "// bar in path3");

    PbProjectSettings settings = PbProjectSettings.getInstance(getProject());
    settings.setImportPathEntries(
        Arrays.asList(
            new ImportPathEntry(VfsUtil.pathToUrl(new File(tempDir, "path1").getPath()), "com/foo"),
            new ImportPathEntry(VfsUtil.pathToUrl(new File(tempDir, "path2").getPath()), "com/foo"),
            new ImportPathEntry(VfsUtil.pathToUrl(new File(tempDir, "path3").getPath()), "")));

    FileResolveProvider resolver = new SettingsFileResolveProvider();
    assertContainsElements(
        resolver.getChildEntries("", getProject()),
        ChildEntry.directory("dir"),
        ChildEntry.directory("com"));
    assertContainsElements(
        resolver.getChildEntries("dir", getProject()), ChildEntry.file("bar.proto"));
    assertContainsElements(
        resolver.getChildEntries("com", getProject()), ChildEntry.directory("foo"));
    assertContainsElements(
        resolver.getChildEntries("com/", getProject()), ChildEntry.directory("foo"));
    assertContainsElements(
        resolver.getChildEntries("com/foo", getProject()), ChildEntry.directory("dir"));
    assertContainsElements(
        resolver.getChildEntries("com/foo/dir", getProject()),
        ChildEntry.file("foo.proto"),
        ChildEntry.file("bar.proto"));
    assertContainsElements(
        resolver.getChildEntries("com/foo/dir/", getProject()),
        ChildEntry.file("foo.proto"),
        ChildEntry.file("bar.proto"));
  }

  public void testCanFindFile() throws Exception {
    File inside1 = new File(tempDir, "inside1");
    File inside2 = new File(tempDir, "inside2");
    File outside = new File(tempDir, "outside");

    File inside1File = new File(inside1, "dir/foo.proto");
    File inside2File = new File(inside2, "dir/foo.proto");
    File outsideFile = new File(outside, "dir/foo.proto");

    FileUtil.writeToFile(inside1File, "// foo in inside1");
    FileUtil.writeToFile(inside2File, "// foo in inside2");
    FileUtil.writeToFile(outsideFile, "// foo in outside");

    PbProjectSettings settings = PbProjectSettings.getInstance(getProject());
    settings.setImportPathEntries(
        Arrays.asList(
            new ImportPathEntry(VfsUtil.pathToUrl(inside1.getPath()), "com/foo"),
            new ImportPathEntry(VfsUtil.pathToUrl(inside2.getPath()), "")));

    FileResolveProvider resolver = new SettingsFileResolveProvider();
    assertTrue(
        resolver.canFindFile(getProject(), notNull(VfsUtil.findFileByIoFile(inside1File, false))));
    assertTrue(
        resolver.canFindFile(getProject(), notNull(VfsUtil.findFileByIoFile(inside2File, false))));
    assertFalse(
        resolver.canFindFile(getProject(), notNull(VfsUtil.findFileByIoFile(outsideFile, false))));
  }
}
