/*
 * Copyright 2015 The authors
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

package com.intellij.lang.ognl;

import com.intellij.lang.InjectableLanguage;
import com.intellij.lang.Language;
import com.intellij.openapi.fileTypes.LanguageFileType;
import org.jetbrains.annotations.NonNls;

/**
 * @author Yann C&eacute;bron
 */
public final class OgnlLanguage extends Language implements InjectableLanguage {

  @NonNls
  public static final String ID = "OGNL";

  @NonNls
  public static final String EXPRESSION_PREFIX = "%{";
  @NonNls
  public static final String EXPRESSION_SUFFIX = "}";

  public static final OgnlLanguage INSTANCE = new OgnlLanguage();

  private OgnlLanguage() {
    super(ID);
  }

  @Override
  public boolean isCaseSensitive() {
    return true;
  }

  @Override
  public LanguageFileType getAssociatedFileType() {
    return OgnlFileType.INSTANCE;
  }

}
