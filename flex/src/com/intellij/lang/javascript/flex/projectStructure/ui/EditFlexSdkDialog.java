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
import com.intellij.openapi.roots.ui.configuration.libraryEditor.LibraryEditor;
import com.intellij.openapi.roots.ui.configuration.libraryEditor.LibraryNameAndLevelPanel;
import com.intellij.openapi.roots.ui.configuration.libraryEditor.LibraryRootsComponent;
import com.intellij.openapi.roots.ui.configuration.libraryEditor.NewLibraryEditor;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.Disposer;
import com.intellij.util.ui.FormBuilder;

import javax.swing.*;
import java.awt.*;

/**
 * @author ksafonov
 */
public class EditFlexSdkDialog extends DialogWrapper {

  private LibraryCompositionSettings mySettings;
  private LibraryRootsComponent myLibraryRootsComponent;
  private FormBuilder myBuilder;

  public EditFlexSdkDialog(Component parent, LibraryCompositionSettings settings, final LibraryEditor libraryEditor) {
    super(parent, true);
    mySettings = settings;
    myLibraryRootsComponent = new LibraryRootsComponent(null, libraryEditor);
    myLibraryRootsComponent.resetProperties();

    Disposer.register(getDisposable(), myLibraryRootsComponent);

    setTitle("Edit " + libraryEditor.getName());

    myBuilder = LibraryNameAndLevelPanel.createFormBuilder();
    init();
  }

  @Override
  protected JComponent createCenterPanel() {
    return myLibraryRootsComponent.getComponent();
  }

  @Override
  protected void doOKAction() {
    myLibraryRootsComponent.applyProperties();
    super.doOKAction();
  }
}
