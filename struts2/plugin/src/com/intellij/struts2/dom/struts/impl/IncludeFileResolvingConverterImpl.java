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

package com.intellij.struts2.dom.struts.impl;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.xml.XmlFile;
import com.intellij.struts2.dom.ConverterUtil;
import com.intellij.struts2.dom.struts.IncludeFileResolvingConverter;
import com.intellij.struts2.dom.struts.model.StrutsModel;
import com.intellij.util.xml.ConvertContext;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Collections;
import java.util.Set;

/**
 * @author Yann CŽbron
 */
public class IncludeFileResolvingConverterImpl extends IncludeFileResolvingConverter {

  @NotNull
  public Collection<? extends XmlFile> getVariants(final ConvertContext context) {
    final StrutsModel strutsModel = ConverterUtil.getStrutsModel(context);
    if (strutsModel == null) {
      return Collections.emptyList();
    }

    final Set<XmlFile> configFiles = strutsModel.getConfigFiles();

    //noinspection SuspiciousMethodCalls
    configFiles.remove(context.getFile().getOriginalFile()); // do not propose current file
    return configFiles;
  }

  public XmlFile fromString(@Nullable @NonNls final String value, final ConvertContext context) {
    if (StringUtil.isEmpty(value)) {
      return null;
    }

    final StrutsModel strutsModel = ConverterUtil.getStrutsModel(context);
    if (strutsModel == null) {
      return null;
    }

    final Set<XmlFile> configFiles = strutsModel.getConfigFiles();
    final String currentFileName = context.getFile().getName();
    for (final XmlFile configFile : configFiles) {
      if (configFile.getName().equals(value) &&
          !currentFileName.equals(value)) { // to trigger error condition on self-inclusion
        return configFile;
      }
    }

    return null;
  }

  public String getErrorMessage(@Nullable final String value, final ConvertContext context) {
    // check if user tries to include current file
    if (context.getFile().getName().equals(value)) {
      return "Recursive inclusion of current file";
    }

    // TODO check for cyclic include

    return "Cannot resolve file ''" + value + "'' (not included in file sets?)";
  }

}