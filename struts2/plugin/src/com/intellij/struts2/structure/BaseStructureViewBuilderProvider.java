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

import com.intellij.ide.structureView.StructureView;
import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.ide.structureView.StructureViewModel;
import com.intellij.ide.structureView.TreeBasedStructureViewBuilder;
import com.intellij.ide.structureView.xml.XmlStructureViewBuilderProvider;
import com.intellij.openapi.Disposable;
import com.intellij.openapi.fileEditor.FileEditor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.xml.DomFileElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Base-class for DOM-model based structure views.
 *
 * @author Yann C&eacute;bron
 */
abstract class BaseStructureViewBuilderProvider implements XmlStructureViewBuilderProvider {

  /**
   * Returns the DomFileElement depending on the root tag of the implementing class.
   *
   * @param xmlFile File to get DomFileElement for.
   * @return null if file doesn't match.
   */
  @Nullable
  protected abstract DomFileElement getFileElement(@NotNull final XmlFile xmlFile);

  /**
   * DomElement classes always having "plus" sign.
   *
   * @return Classes.
   */
  protected abstract Class[] getAlwaysPlus();

  /**
   * DomElement classes always being leaf.
   *
   * @return Classes.
   */
  protected abstract Class[] getAlwaysLeaf();

  @Nullable
  public StructureViewBuilder createStructureViewBuilder(@NotNull final XmlFile xmlFile) {

    final DomFileElement fileElement = getFileElement(xmlFile);

    if (fileElement == null) {
      return null;
    }

    return new TreeBasedStructureViewBuilder() {

      @NotNull
      public StructureView createStructureView(final FileEditor fileEditor, final Project project) {
        final StructureViewModel model = createStructureViewModel();
        StructureViewComponent view = new StructureViewComponent(fileEditor, model, project);
        Disposer.register(view, new Disposable() {
          @Override
          public void dispose() {
            model.dispose();
          }
        });
        return view;
      }

      @NotNull
      public StructureViewModel createStructureViewModel() {
        return new StructureViewTreeModel(xmlFile, fileElement.getRootElement(), getAlwaysPlus(), getAlwaysLeaf());
      }

    };
  }

}
