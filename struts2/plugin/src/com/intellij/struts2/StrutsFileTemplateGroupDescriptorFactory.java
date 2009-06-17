/*
 * Copyright 2008 The authors
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

import com.intellij.ide.fileTemplates.FileTemplateDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptorFactory;
import org.jetbrains.annotations.NonNls;

/**
 * Provides filetemplates for struts.mxl/validator.xml files.
 *
 * @author Yann C&eacute;bron
 */
public class StrutsFileTemplateGroupDescriptorFactory implements FileTemplateGroupDescriptorFactory {

  /**
   * Template for {@code 2.0.x}.
   */
  @NonNls
  public static final String STRUTS_2_0_XML = "struts.xml";

  /**
   * Template for {@code 2.1.x}.
   */
  @NonNls
  public static final String STRUTS_2_1_XML = "struts_2_1.xml";

  public FileTemplateGroupDescriptor getFileTemplatesDescriptor() {
    final FileTemplateGroupDescriptor group = new FileTemplateGroupDescriptor(StrutsBundle.message("struts2"),
                                                                              StrutsIcons.ACTION);
    group.addTemplate(new FileTemplateDescriptor(STRUTS_2_0_XML,
                                                 StrutsIcons.STRUTS_CONFIG_FILE_ICON));
    group.addTemplate(new FileTemplateDescriptor(STRUTS_2_1_XML,
                                                 StrutsIcons.STRUTS_CONFIG_FILE_ICON));

    group.addTemplate(new FileTemplateDescriptor("validator.xml", StrutsIcons.VALIDATION_CONFIG_FILE_ICON));
    return group;
  }

}