/*
 * Copyright 2018 The authors
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

package com.intellij.lang.ognl.parsing;

import com.intellij.lang.ognl.OgnlFileType;
import com.intellij.lang.ognl.OgnlParserDefinition;
import com.intellij.lang.ognl.OgnlTestUtils;
import com.intellij.openapi.application.PathManager;
import com.intellij.testFramework.ParsingTestCase;
import org.jetbrains.annotations.NonNls;

import java.io.File;

/**
 * Testing parser "oddities" and error messages.
 *
 * @author Yann C&eacute;bron
 */
public abstract class OgnlParsingTestCase extends ParsingTestCase {

  protected OgnlParsingTestCase(@NonNls final String dataPath) {
    super(dataPath, OgnlFileType.INSTANCE.getDefaultExtension(), new OgnlParserDefinition());
  }

  @Override
  protected String getTestDataPath() {
    return PathManager.getHomePath().replace(File.separatorChar, '/') + OgnlTestUtils.OGNL_TEST_DATA + "/psi";
  }
}
