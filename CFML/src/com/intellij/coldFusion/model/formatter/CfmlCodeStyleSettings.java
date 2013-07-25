/*
 * Copyright 2000-2013 JetBrains s.r.o.
 *
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
package com.intellij.coldFusion.model.formatter;

import com.intellij.psi.codeStyle.CodeStyleSettings;
import com.intellij.psi.codeStyle.CustomCodeStyleSettings;

/**
 * Created by IntelliJ IDEA.
 * User: Nadya.Zabrodina
 */
public class CfmlCodeStyleSettings extends CustomCodeStyleSettings {

  public boolean INDENT_CODE_IN_CFML_TAGS = true;
  public boolean ALIGN_KEY_VALUE_PAIRS = false;
  public boolean ALIGN_CFMLDOC_PARAM_NAMES = false;
  public boolean ALIGN_CFMLDOC_COMMENTS = false;
  public boolean ALIGN_ASSIGNMENTS = false;
  public boolean CONCAT_SPACES = true;

  public boolean CFMLDOC_BLANK_LINE_BEFORE_TAGS = false;
  public boolean CFMLDOC_BLANK_LINES_AROUND_PARAMETERS = false;

  public CfmlCodeStyleSettings(CodeStyleSettings container) {
    super("CfmlCodeStyleSettings", container);
  }
}
