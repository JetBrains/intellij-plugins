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

package com.intellij.lang.ognl.template;

import com.intellij.codeInsight.template.impl.DefaultLiveTemplatesProvider;

/**
 * Provides builtin live templates.
 *
 * @author Yann C&eacute;bron
 */
public class OgnlDefaultLiveTemplatesProvider implements DefaultLiveTemplatesProvider {

  @Override
  public String[] getDefaultLiveTemplateFiles() {
    return new String[]{"/liveTemplates/ognl"};
  }

  @Override
  public String[] getHiddenLiveTemplateFiles() {
    return null;
  }

}