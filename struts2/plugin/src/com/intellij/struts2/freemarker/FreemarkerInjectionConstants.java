/*
 * Copyright 2011 The authors
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

package com.intellij.struts2.freemarker;

import com.intellij.freemarker.psi.directives.FtlMacro;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.patterns.PatternCondition;
import com.intellij.struts2.StrutsConstants;
import com.intellij.util.ProcessingContext;
import org.jetbrains.annotations.NotNull;

/**
 * Useful patterns for language injection.
 *
 * @author Yann C&eacute;bron
 */
public final class FreemarkerInjectionConstants {

  private FreemarkerInjectionConstants() {
  }

  public static final PatternCondition<FtlMacro> TAGLIB_PREFIX = new PatternCondition<>("S2 taglib prefix") {
    @Override
    public boolean accepts(@NotNull final FtlMacro ftlMacro, final ProcessingContext processingContext) {
      final String name = ftlMacro.getName();
      return StringUtil.startsWith(name, '@' + StrutsConstants.TAGLIB_STRUTS_UI_PREFIX + '.') ||
             StringUtil.startsWith(name, '@' + StrutsConstants.TAGLIB_JQUERY_PLUGIN_PREFIX + '.') ||
             StringUtil.startsWith(name, '@' + StrutsConstants.TAGLIB_JQUERY_RICHTEXT_PLUGIN_PREFIX + '.');
    }
  };

}