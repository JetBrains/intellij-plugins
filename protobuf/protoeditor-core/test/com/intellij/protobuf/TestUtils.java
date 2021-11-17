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
package com.intellij.protobuf;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.extensions.LoadingOrder;
import com.intellij.openapi.fileTypes.FileTypeManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Key;
import com.intellij.protobuf.ide.util.ResourceUtil;
import com.intellij.protobuf.lang.PbFileType;
import com.intellij.protobuf.lang.resolve.FileResolveProvider;
import com.intellij.protobuf.lang.resolve.LocalRootsFileResolveProvider;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;
import java.util.Arrays;

import static org.junit.Assert.assertNotNull;

/** Various testing utility methods. */
public final class TestUtils {

  public static final String OPENSOURCE_DESCRIPTOR_PATH = "google/protobuf/descriptor.proto";

  private static final Key<FileResolveProvider> TEST_FILE_RESOLVE_PROVIDER =
    Key.create("TEST_FILE_RESOLVE_PROVIDER");

  public static String getTestdataPath() {
    return "contrib/protobuf/protoeditor-core/testData/";
  }

  public static void addTestFileResolveProvider(Project project, Disposable disposable) {
    addTestFileResolveProvider(project, null, disposable);
  }

  public static void addTestFileResolveProvider(
    Project project, String descriptorPath, @NotNull Disposable disposable) {
    FileResolveProvider provider = new LocalRootsFileResolveProvider(descriptorPath);
    // Make sure the test provider comes first, before the "settings" provider.
    project.getExtensionArea()
      .getExtensionPoint(FileResolveProvider.EP_NAME)
      .registerExtension(provider, LoadingOrder.readOrder("FIRST, BEFORE settings"), disposable);
    project.putUserData(TEST_FILE_RESOLVE_PROVIDER, provider);
  }

  public static String getOpensourceDescriptorText() throws IOException {
    return ResourceUtil.readPathAsString("/include/google/protobuf/descriptor.proto");
  }

  @NotNull
  public static <T> T notNull(@Nullable T object) {
    assertNotNull(object);
    return object;
  }

  public static void registerTestdataFileExtension() {
    ApplicationManager.getApplication()
      .runWriteAction(
        () ->
          FileTypeManager.getInstance()
            .associatePattern(PbFileType.INSTANCE, "*.proto.testdata"));
  }

  public static String makeFileWithSyntaxAndPackage(
    String syntaxForTest, String packageForTest, String... fileContents) {
    return String.join(
      "\n",
      Iterables.concat(
        ImmutableList.of(
          String.format("syntax = \"%s\";", syntaxForTest),
          String.format("package %s;", packageForTest)),
        Arrays.asList(fileContents)));
  }

  private TestUtils() { }
}
