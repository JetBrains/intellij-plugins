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

package com.intellij.struts2;

import com.intellij.ide.presentation.PresentationProvider;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.struts2.dom.validator.Field;
import com.intellij.struts2.dom.validator.FieldValidator;
import com.intellij.struts2.dom.validator.Message;
import com.intellij.struts2.dom.validator.Validators;
import com.intellij.struts2.dom.validator.config.ValidatorConfig;
import com.intellij.util.xml.DomUtil;

import javax.swing.*;

/**
 * Presentation icon/name for Validation DOM elements.
 *
 * @author Yann C&eacute;bron
 */
public class Struts2ValidationPresentationProvider extends PresentationProvider {

  @Override
  public Icon getIcon(final Object o) {

    if (o instanceof Validators) {
      return StrutsIcons.VALIDATION_CONFIG_FILE;
    }

    return null;
  }

  @Override
  public String getName(final Object o) {

    if (o instanceof Validators) {
      return DomUtil.getFile(((Validators) o)).getName();
    }

    if (o instanceof Field) {
      return ((Field) o).getName().getStringValue();
    }

    if (o instanceof FieldValidator) {
      final ValidatorConfig validatorConfig = ((FieldValidator) o).getType().getValue();
      return validatorConfig != null ? validatorConfig.getName().getStringValue() : null;
    }

    if (o instanceof Message) {
      final String key = ((Message) o).getKey().getStringValue();
      return StringUtil.isNotEmpty(key) ? key : ((Message) o).getValue();
    }

    return null;
  }

}