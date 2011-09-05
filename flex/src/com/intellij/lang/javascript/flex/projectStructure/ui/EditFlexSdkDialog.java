/*
 * Copyright 2000-2010 JetBrains s.r.o.
 *
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
package com.intellij.lang.javascript.flex.projectStructure.ui;

import com.intellij.facet.impl.ui.libraries.LibraryCompositionSettings;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.roots.ui.configuration.libraryEditor.*;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.Disposer;
import com.intellij.util.ui.FormBuilder;

import javax.swing.*;
import java.awt.*;

/**
 * @author ksafonov
 */
public class EditFlexSdkDialog extends LibraryEditorDialogBase {

  public EditFlexSdkDialog(Project project, LibraryEditor libraryEditor, Component parent) {
    super(parent, new LibraryRootsComponent(project, libraryEditor));
    setTitle("Configure " + libraryEditor.getName());
    init();
  }

  @Override
  protected JComponent createNorthPanel() {
    // create name editor, but hide it
    super.createNorthPanel();
    return null;
  }

  @Override
  protected boolean shouldCheckName(String newName) {
    return false;
  }
}
