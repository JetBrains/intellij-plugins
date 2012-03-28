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

import com.google.common.base.Joiner;
import com.intellij.openapi.fileTypes.ExtensionFileNameMatcher;
import com.intellij.openapi.fileTypes.FileNameMatcher;
import com.intellij.openapi.fileTypes.FileTypeConsumer;
import com.intellij.openapi.fileTypes.FileTypeFactory;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class JstdConfigFileLoader extends FileTypeFactory {

  @Override
  public void createFileTypes(@NotNull FileTypeConsumer consumer) {
    JstdConfigFileType jstdConfigFileType = JstdConfigFileType.INSTANCE;
    FileNameMatcher[] matchers = {
      new ExtensionFileNameMatcher(JstdConfigFileType.INSTANCE.getDefaultExtension()),
      new CustomFileNameMatcher()
    };
    consumer.consume(jstdConfigFileType, matchers);
  }

  /**
   * Matches some predefined fileNames.
   * The motivation is that configuration file is often named 'jsTestDriver.conf'.
   * Examples of matched fileNames: 'jsTestDriver.conf', 'jsTestDriver.yaml', 'jstd.yml', 'jsTestDriver-coverage.conf', etc
   */
  private static class CustomFileNameMatcher implements FileNameMatcher {

    private static final String[] PREFIXES = {"jsTestDriver", "js-test-driver", "js_test_driver", "jstd"};
    private static final String[] DOT_SUFFIXES = {".conf", ".yml", ".yaml"};
    private static final String COMMON_PREFIX;
    private static final int COMMON_PREFIX_LENGTH;
    static {
      String common = PREFIXES[0];
      for (int i = 1; i < PREFIXES.length; i++) {
        common = StringUtil.commonPrefix(common, PREFIXES[i]);
      }
      COMMON_PREFIX = common;
      COMMON_PREFIX_LENGTH = COMMON_PREFIX.length();
    }

    @Override
    public boolean accept(@NonNls @NotNull String fileName) {
      if (COMMON_PREFIX_LENGTH > 0) {
        if (!fileName.startsWith(COMMON_PREFIX)) {
          return false;
        }
      }
      for (String prefix : PREFIXES) {
        if (fileName.startsWith(prefix)) {
          if (hasSuitableExtension(fileName)) {
            return true;
          }
        }
      }
      return false;
    }

    @NotNull
    @Override
    public String getPresentableString() {
      Joiner joiner = Joiner.on("|");
      return "(" + joiner.join(PREFIXES) + ")*(" + joiner.join(DOT_SUFFIXES) + ")";
    }

    private static boolean hasSuitableExtension(@NotNull String fileName) {
      for (String dotSuffix : DOT_SUFFIXES) {
        if (fileName.endsWith(dotSuffix)) {
          return true;
        }
      }
      return false;
    }
  }

}
