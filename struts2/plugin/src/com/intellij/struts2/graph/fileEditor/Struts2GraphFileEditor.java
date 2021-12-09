/*
 * Copyright 2015 The authors
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

package com.intellij.struts2.graph.fileEditor;

import com.intellij.ide.structureView.StructureViewBuilder;
import com.intellij.openapi.application.ReadAction;
import com.intellij.openapi.graph.builder.components.GraphStructureViewBuilderSetup;
import com.intellij.openapi.progress.ProgressManager;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Disposer;
import com.intellij.openapi.util.NotNullLazyValue;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.xml.DomElement;
import com.intellij.util.xml.ui.PerspectiveFileEditor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

/**
 * @author Yann C&eacute;bron
 */
public class Struts2GraphFileEditor extends PerspectiveFileEditor {

  private Struts2GraphComponent myComponent;
  private final XmlFile myXmlFile;

  private final @NotNull NotNullLazyValue<StructureViewBuilder> myStructureViewBuilder =
    NotNullLazyValue.atomicLazy(() -> GraphStructureViewBuilderSetup.setupFor(getStruts2GraphComponent().getBuilder(), null));

  public Struts2GraphFileEditor(final Project project, final VirtualFile file) {
    super(project, file);

    final PsiFile psiFile = getPsiFile();
    assert psiFile instanceof XmlFile;

    myXmlFile = (XmlFile)psiFile;
  }

  @Override
  @Nullable
  protected DomElement getSelectedDomElement() {
    final List<DomElement> selectedDomElements = getStruts2GraphComponent().getSelectedDomElements();

    return selectedDomElements.size() > 0 ? selectedDomElements.get(0) : null;
  }

  @Override
  protected void setSelectedDomElement(final DomElement domElement) {
    getStruts2GraphComponent().setSelectedDomElement(domElement);
  }

  @Override
  @NotNull
  protected JComponent createCustomComponent() {
    return getStruts2GraphComponent();
  }

  @Override
  @Nullable
  public JComponent getPreferredFocusedComponent() {
    return getStruts2GraphComponent().getBuilder().getView().getJComponent();
  }

  @Override
  public void commit() {
  }

  @Override
  public void reset() {
    getStruts2GraphComponent().getBuilder().queueUpdate();
  }

  @Override
  @NotNull
  public String getName() {
    return "Graph";
  }

  @Override
  public StructureViewBuilder getStructureViewBuilder() {
    return myStructureViewBuilder.getValue();
  }

  private Struts2GraphComponent getStruts2GraphComponent() {
    if (myComponent == null) {
      myComponent = createGraphComponent();
      Disposer.register(this, myComponent);
    }
    return myComponent;
  }


  /**
   * Creates graph component while showing modal wait dialog.
   *
   * @return new instance.
   */
  private Struts2GraphComponent createGraphComponent() {
    final Struts2GraphComponent[] graphComponent = {null};
    ProgressManager.getInstance().runProcessWithProgressSynchronously(
      (Runnable)() -> graphComponent[0] = ReadAction.compute(() -> new Struts2GraphComponent(myXmlFile)), "Generating Graph", false, myXmlFile.getProject());


    return graphComponent[0];
  }
}