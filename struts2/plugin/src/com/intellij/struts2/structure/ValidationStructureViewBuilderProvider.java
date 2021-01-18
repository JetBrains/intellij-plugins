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
package com.intellij.struts2.structure;

import com.intellij.psi.xml.XmlFile;
import com.intellij.struts2.dom.params.Param;
import com.intellij.struts2.dom.validator.Field;
import com.intellij.struts2.dom.validator.FieldValidator;
import com.intellij.struts2.dom.validator.Message;
import com.intellij.struts2.dom.validator.Validators;
import com.intellij.struts2.dom.validator.config.ValidatorConfig;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.DomManager;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Provides structure view for validation.xml files.
 *
 * @author Yann C&eacute;bron
 */
final class ValidationStructureViewBuilderProvider extends BaseStructureViewBuilderProvider {
  @Override
  @Nullable
  protected DomFileElement getFileElement(@NotNull final XmlFile xmlFile) {
    final DomManager domManager = DomManager.getDomManager(xmlFile.getProject());
    return domManager.getFileElement(xmlFile, Validators.class);
  }

  @Override
  protected Class[] getAlwaysPlus() {
    return new Class[]{Field.class, FieldValidator.class};
  }

  @Override
  protected Class[] getAlwaysLeaf() {
    return new Class[]{Param.class, Message.class, ValidatorConfig.class};
  }

}