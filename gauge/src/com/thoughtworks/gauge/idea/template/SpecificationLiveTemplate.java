/*
 * Copyright (C) 2020 ThoughtWorks, Inc.
 *
 *  Licensed under the Apache License, Version 2.0 (the "License");
 *  you may not use this file except in compliance with the License.
 *  You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *  Unless required by applicable law or agreed to in writing, software
 *  distributed under the License is distributed on an "AS IS" BASIS,
 *  WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *  See the License for the specific language governing permissions and
 *  limitations under the License.
 */

package com.thoughtworks.gauge.idea.template;

import com.intellij.ide.fileTemplates.FileTemplateDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptor;
import com.intellij.ide.fileTemplates.FileTemplateGroupDescriptorFactory;
import com.thoughtworks.gauge.GaugeBundle;
import com.thoughtworks.gauge.language.ConceptFileType;
import com.thoughtworks.gauge.language.SpecFileType;
import icons.GaugeIcons;

final class SpecificationLiveTemplate implements FileTemplateGroupDescriptorFactory {
  @Override
  public FileTemplateGroupDescriptor getFileTemplatesDescriptor() {
    FileTemplateGroupDescriptor descriptor = new FileTemplateGroupDescriptor(GaugeBundle.message("file.templates.group.specification"), GaugeIcons.Gauge);
    descriptor.addTemplate(new FileTemplateDescriptor("Specification.spec", SpecFileType.INSTANCE.getIcon()));
    descriptor.addTemplate(new FileTemplateDescriptor("Concept.cpt", ConceptFileType.INSTANCE.getIcon()));
    return descriptor;
  }
}
