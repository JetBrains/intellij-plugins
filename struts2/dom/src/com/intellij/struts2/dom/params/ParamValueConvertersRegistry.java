/*
 * Copyright 2013 The authors
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
package com.intellij.struts2.dom.params;

import com.intellij.util.xml.converters.values.GenericDomValueConvertersRegistry;

/**
 * Registry of all converters used by {@link ParamValueConverter}.
 *
 * @author Yann C&eacute;bron
 */
public final class ParamValueConvertersRegistry extends GenericDomValueConvertersRegistry {

  private static final ParamValueConvertersRegistry INSTANCE = new ParamValueConvertersRegistry();

  public static ParamValueConvertersRegistry getInstance() {
    return INSTANCE;
  }

  private ParamValueConvertersRegistry() {
    registerBuiltinValueConverters();
  }

  private void registerBuiltinValueConverters() {
    registerDefaultConverters();
  }
}
