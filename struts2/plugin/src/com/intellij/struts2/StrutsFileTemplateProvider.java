/*
 * Copyright 2016 The authors
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

import com.intellij.ide.fileTemplates.FileTemplate;
import com.intellij.ide.fileTemplates.FileTemplateManager;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.project.Project;
import com.intellij.struts2.facet.ui.StrutsVersionDetector;
import com.intellij.util.text.VersionComparatorUtil;
import org.jetbrains.annotations.NotNull;

/**
 * @author Yann C&eacute;bron
 */
public class StrutsFileTemplateProvider {

  private final String myVersionName;
  private final boolean my21orNewer;

  public StrutsFileTemplateProvider(Module module) {
    myVersionName = StrutsVersionDetector.detectStrutsVersion(module);
    my21orNewer = isNewerThan("2.1");
  }

  @NotNull
  public FileTemplate determineFileTemplate(Project project) {
    String template;
    if (isNewerThan("2.5")) {
      template = StrutsFileTemplateGroupDescriptorFactory.STRUTS_2_5_XML;
    }
    else if (isNewerThan("2.3")) {
      template = StrutsFileTemplateGroupDescriptorFactory.STRUTS_2_3_XML;
    }
    else if (my21orNewer) {
      template = isNewerThan("2.1.7") ?
                 StrutsFileTemplateGroupDescriptorFactory.STRUTS_2_1_7_XML :
                 StrutsFileTemplateGroupDescriptorFactory.STRUTS_2_1_XML;
    }
    else {
      template = StrutsFileTemplateGroupDescriptorFactory.STRUTS_2_0_XML;
    }

    final FileTemplateManager fileTemplateManager = FileTemplateManager.getInstance(project);
    return fileTemplateManager.getJ2eeTemplate(template);
  }

  public boolean is21orNewer() {
    return my21orNewer;
  }

  private boolean isNewerThan(String versionName) {
    return VersionComparatorUtil.compare(myVersionName, versionName) > 0;
  }
}
