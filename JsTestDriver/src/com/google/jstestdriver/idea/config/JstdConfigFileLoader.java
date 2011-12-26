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

import com.intellij.openapi.fileTypes.*;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public class JstdConfigFileLoader extends FileTypeFactory {

  @Override
  public void createFileTypes(@NotNull FileTypeConsumer consumer) {
    JstdConfigFileType jstdConfigFileType = JstdConfigFileType.INSTANCE;
    FileNameMatcher[] matchers = getFileNameMatchers();
    consumer.consume(jstdConfigFileType, matchers);
  }

  private static FileNameMatcher[] getFileNameMatchers() {
    List<FileNameMatcher> matchers = new ArrayList<FileNameMatcher>();
    matchers.add(new ExtensionFileNameMatcher(JstdConfigFileType.INSTANCE.getDefaultExtension()));
    String[] namesWithoutExt = {"jsTestDriver", "js-test-driver", "js_test_driver", "jstd"};
    String[] extensions = {"conf", "yml", "yaml"};
    for (String nameWithoutExt : namesWithoutExt) {
      for (String ext : extensions) {
        ExactFileNameMatcher fileNameMatcher = new ExactFileNameMatcher(nameWithoutExt + "." + ext);
        matchers.add(fileNameMatcher);
      }
    }
    return matchers.toArray(new FileNameMatcher[matchers.size()]);
  }

}
