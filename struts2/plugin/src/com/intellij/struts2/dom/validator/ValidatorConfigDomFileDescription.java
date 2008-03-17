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

package com.intellij.struts2.dom.validator;

import com.intellij.psi.xml.XmlTag;
import com.intellij.struts2.StrutsConstants;
import com.intellij.struts2.dom.StrutsDomConstants;
import com.intellij.struts2.dom.validator.config.ValidatorsConfig;
import com.intellij.util.NotNullFunction;
import com.intellij.util.xml.DomFileDescription;
import org.jetbrains.annotations.NotNull;

import java.util.Arrays;
import java.util.List;

/**
 * <code>validators.xml</code> DOM-Model files.
 *
 * @author Yann CŽbron
 */
public class ValidatorConfigDomFileDescription extends DomFileDescription<ValidatorsConfig> {

  private static final List<String> VALIDATOR_CONFIG_NAMESPACES = Arrays.asList(StrutsConstants.VALIDATOR_CONFIG_DTD_URI,
                                                                                StrutsConstants.VALIDATOR_CONFIG_DTD_ID);

  public ValidatorConfigDomFileDescription() {
    super(ValidatorsConfig.class, ValidatorsConfig.TAG_NAME);
  }

  protected void initializeFileDescription() {
    registerNamespacePolicy(StrutsDomConstants.VALIDATOR_CONFIG_NAMESPACE_KEY, new NotNullFunction<XmlTag, List<String>>() {
      @NotNull
      public List<String> fun(final XmlTag tag) {
        return VALIDATOR_CONFIG_NAMESPACES;
      }
    });
  }

}