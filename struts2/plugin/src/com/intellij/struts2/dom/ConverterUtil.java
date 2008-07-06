/*
 * Copyright 2007 The authors
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

package com.intellij.struts2.dom;

import com.intellij.psi.xml.XmlFile;
import com.intellij.struts2.dom.struts.model.StrutsManager;
import com.intellij.struts2.dom.struts.model.StrutsModel;
import com.intellij.util.xml.ConvertContext;
import org.jetbrains.annotations.Nullable;

/**
 * Utility methods for DOM-Converters.
 *
 * @author Yann C&eacute;bron
 */
public final class ConverterUtil {

  private ConverterUtil() {
  }

  /**
   * Gets the StrutsModel for the current context (=file).
   *
   * @param context Invoking context.
   * @return <code>null</code> if no StrutsModel found by current file (e.g. not in any fileset).
   */
  @Nullable
  public static StrutsModel getStrutsModel(final ConvertContext context) {
    final XmlFile xmlFile = context.getFile();
    return StrutsManager.getInstance(xmlFile.getProject()).getModelByFile(xmlFile);
  }

}