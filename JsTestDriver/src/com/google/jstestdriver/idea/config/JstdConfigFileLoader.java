/*
 * Copyright 2009 Google Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License. You may obtain a copy of
 * the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under
 * the License.
 */
package com.google.jstestdriver.idea.config;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.intellij.openapi.fileTypes.ExtensionFileNameMatcher;
import com.intellij.openapi.fileTypes.FileNameMatcher;
import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import com.intellij.openapi.util.io.FileUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.concurrent.atomic.AtomicInteger;

public class JstdConfigFileLoader extends FileTypeFactory {

  public static final AtomicInteger ourAcceptCount = new AtomicInteger(0);

  @Override
  public void createFileTypes(@NotNull FileTypeConsumer consumer) {
    JstdConfigFileType jstdConfigFileType = JstdConfigFileType.INSTANCE;
    FileNameMatcher[] matchers = new FileNameMatcher[] {
      new MyExtensionFileNameMatcher(jstdConfigFileType.getDefaultExtension()),
      new CustomFileNameMatcher()
    };
    consumer.consume(jstdConfigFileType, matchers);
  }

  /**
   * Matches some predefined fileNames.
   * The motivation is that configuration file is often named 'jsTestDriver.conf'.
   * Examples of matched fileNames: 'jsTestDriver.conf', 'jsTestDriver.yaml', 'jstd.yml', etc
   */
  private static class CustomFileNameMatcher implements FileNameMatcher {

    private static final ImmutableSet<String> FILE_NAMES_WITHOUT_EXTENSION;
    private static final ImmutableSet<String> FILE_EXTENSIONS;

    static {
      Function<String, String> lower = new Function<String, String>() {
        @Override
        public String apply(String s) {
          return s.toLowerCase();
        }
      };
      FILE_NAMES_WITHOUT_EXTENSION = ImmutableSet.copyOf(Iterables.transform(Arrays.asList(
        "jsTestDriver", "js-test-driver", "js_test_driver", "jstd"
      ), lower));
      FILE_EXTENSIONS = ImmutableSet.copyOf(Iterables.transform(Arrays.asList(
          "conf", "yml", "yaml"
      ), lower));
    }


    @Override
    public boolean accept(@NonNls @NotNull String fileName) {
      ourAcceptCount.incrementAndGet();
      String extension = FileUtil.getExtension(fileName);
      if (FILE_EXTENSIONS.contains(extension)) {
        String fileNameWithoutExtention = FileUtil.getNameWithoutExtension(fileName);
        if (isSuitableNameWithoutExtension(fileNameWithoutExtention)) {
          return true;
        }
      }
      return false;
    }

    @NotNull
    @Override
    public String getPresentableString() {
      Joiner joiner = Joiner.on("|");
      return "(" + joiner.join(FILE_NAMES_WITHOUT_EXTENSION) + ").(" + joiner.join(FILE_EXTENSIONS) + ")";
    }

    private static boolean isSuitableNameWithoutExtension(@NotNull String fileNameWithoutExtension) {
      String lowerCased = fileNameWithoutExtension.toLowerCase();
      for (String prefix : FILE_NAMES_WITHOUT_EXTENSION) {
        if (lowerCased.startsWith(prefix)) {
          return true;
        }
      }
      return false;
    }
  }

  private static class MyExtensionFileNameMatcher extends ExtensionFileNameMatcher {

    public MyExtensionFileNameMatcher(@NotNull @NonNls String extension) {
      super(extension);
    }

    public boolean accept(@NotNull @NonNls String fileName) {
      ourAcceptCount.incrementAndGet();
      return super.accept(fileName);
    }

  }

}
