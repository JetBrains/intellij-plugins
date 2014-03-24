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
import com.intellij.openapi.fileTypes.*;
import com.intellij.openapi.util.text.StringUtil;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

public class JstdConfigFileTypeFactory extends FileTypeFactory {

  @Override
  public void createFileTypes(@NotNull FileTypeConsumer consumer) {
    consumer.consume(
      JstdConfigFileType.INSTANCE,
      new ExtensionFileNameMatcher(JstdConfigFileType.INSTANCE.getDefaultExtension()),
      new JstdPredefinedFileNameMatcher()
    );
  }

  /**
   * Accepts file names that match a following pattern:
   * (prefix_1 | prefix_2 | ... | prefix_N).*(suffix_1 | suffix_2 | ... | suffix_N).
   * <p/>
   * The motivation is that configuration file name may have some common extension ('*.conf', '*.yml', etc),
   * that could be applicable not only for JsTestDriver configuration files.
   * That is why matching is restricted by requiring file names to be started with one of predefined prefixes.
   * <p/>
   * For instance following file names will be accepted:
   *    'jsTestDriver.conf', 'jsTestDriver.yaml', 'jstd.yml', 'jsTestDriver-coverage.conf'.
   */
  private static class JstdPredefinedFileNameMatcher extends FileNameMatcherEx {

    private static final String[] PREFIXES = {"jsTestDriver", "js-test-driver", "js_test_driver", "jstd"};
    private static final String[] DOT_SUFFIXES = {".conf", ".yml", ".yaml"};
    private static final String COMMON_PREFIX;
    private static final int COMMON_PREFIX_LENGTH;
    static {
      String common = PREFIXES[0];
      for (String prefix : PREFIXES) {
        common = StringUtil.commonPrefix(common, prefix);
      }
      COMMON_PREFIX = common;
      COMMON_PREFIX_LENGTH = COMMON_PREFIX.length();
    }

    @Override
    public boolean acceptsCharSequence(@NonNls @NotNull CharSequence fileName) {
      if (COMMON_PREFIX_LENGTH > 0) {
        // performance optimization
        if (!StringUtil.startsWith(fileName, COMMON_PREFIX)) {
          return false;
        }
      }
      for (String prefix : PREFIXES) {
        if (StringUtil.startsWith(fileName, prefix)) {
          return hasSuitableExtension(fileName);
        }
      }
      return false;
    }

    @NotNull
    @Override
    public String getPresentableString() {
      Joiner joiner = Joiner.on("|");
      return "(" + joiner.join(PREFIXES) + ").*(" + joiner.join(DOT_SUFFIXES) + ")";
    }

    private static boolean hasSuitableExtension(@NotNull CharSequence fileName) {
      for (String dotSuffix : DOT_SUFFIXES) {
        if (StringUtil.endsWith(fileName, dotSuffix)) {
          return true;
        }
      }
      return false;
    }
  }

}
