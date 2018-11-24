/*
 * Copyright 2013 The authors
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

import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.ide.structureView.StructureViewModel;
import com.intellij.ide.structureView.xml.XmlStructureViewBuilderProvider;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.ConstantFunction;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.DomFileElement;
import com.intellij.util.xml.DomService;
import com.intellij.util.xml.structure.DomStructureViewBuilder;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Base-class for DOM-model based structure views.
 *
 * @author Yann C&eacute;bron
 */
abstract class BaseStructureViewBuilderProvider implements XmlStructureViewBuilderProvider {

  static final ConstantFunction<DomElement, DomService.StructureViewMode> ALWAYS_SHOW =
    new ConstantFunction<>(DomService.StructureViewMode.SHOW);

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

  @Override
  @Nullable
  public StructureViewBuilder createStructureViewBuilder(@NotNull final XmlFile xmlFile) {

    final DomFileElement fileElement = getFileElement(xmlFile);

    if (fileElement == null) {
      return null;
    }

    return new DomStructureViewBuilder(xmlFile, ALWAYS_SHOW) {

      @NotNull
      @Override
      public StructureViewModel createStructureViewModel(@Nullable Editor editor) {
        return new StructureViewTreeModel(xmlFile, editor, getAlwaysPlus(), getAlwaysLeaf(), ALWAYS_SHOW);
      }
    };
  }
}
