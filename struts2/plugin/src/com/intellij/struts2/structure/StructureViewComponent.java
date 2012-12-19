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

package com.intellij.struts2.structure;

import com.intellij.ide.structureView.StructureViewModel;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.Project;
import com.intellij.ui.TreeSpeedSearch;

/**
 * Adds SpeedSearch (Alt+F3) to the structure view.
 *
 * @author Yann C&eacute;bron
 */
class StructureViewComponent extends com.intellij.ide.structureView.newStructureView.StructureViewComponent {

  StructureViewComponent(final FileEditor fileEditor,
                         final StructureViewModel structureViewModel,
                         final Project project) {
    super(fileEditor, structureViewModel, project);
    new TreeSpeedSearch(getTree());
  }

}