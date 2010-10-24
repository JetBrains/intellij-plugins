/*
 * Copyright 2010 The authors
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.intellij.struts2;

import com.intellij.openapi.application.PathManager;
import com.intellij.testFramework.UsefulTestCase;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;

import java.io.File;

/**
 * @author Yann C&eacute;bron
 */
public abstract class BasicStrutsTestCase extends UsefulTestCase {

  /**
   * Return absolute full path to the test data. Not intended to be overridden.
   *
   * @return absolute path to the test data.
   */
  @NonNls
  @NotNull
  protected final String getTestDataPath() {
    return getTestDataBasePath() + getTestDataLocation();
  }

  /**
   * Returns the base path for all test data directories.
   *
   * @return "./plugin/testData/"
   */
  @NonNls
  @NotNull
  protected final String getTestDataBasePath() {
    return PathManager.getHomePath().replace(File.separatorChar, '/') + "/contrib/struts2/plugin/testData/";
  }

  /**
   * Sets the relative directory for test data to use.
   *
   * @return relative directory location.
   */
  @NonNls
  @NotNull
  protected abstract String getTestDataLocation();

}
