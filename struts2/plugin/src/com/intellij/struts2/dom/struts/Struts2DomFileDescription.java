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

package com.intellij.struts2.dom.struts;

import com.intellij.openapi.util.Iconable;
import com.intellij.struts2.StrutsConstants;
import com.intellij.struts2.StrutsIcons;
import com.intellij.struts2.dom.StrutsDomConstants;
import com.intellij.util.xml.DomFileDescription;

import javax.swing.*;

/**
 * {@code struts.xml} DOM-Model files.
 *
 * @author Yann C&eacute;bron
 */
public class Struts2DomFileDescription extends DomFileDescription<StrutsRoot> {

  public Struts2DomFileDescription() {
    super(StrutsRoot.class, StrutsRoot.TAG_NAME);
  }

  @Override
  protected void initializeFileDescription() {
    registerNamespacePolicy(StrutsDomConstants.STRUTS_NAMESPACE_KEY,
                            StrutsConstants.STRUTS_DTDS);
  }

  @Override
  public Icon getFileIcon(@Iconable.IconFlags int flags) {
    return StrutsIcons.STRUTS_CONFIG_FILE;
  }

}