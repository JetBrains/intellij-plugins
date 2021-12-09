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

package com.thoughtworks.gauge.extract.stepBuilder;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.SelectionModel;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.thoughtworks.gauge.language.ConceptFileType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class StepsBuilder {
  private static final Logger LOG = Logger.getInstance(StepsBuilder.class);

  protected final Editor editor;
  protected final PsiFile psiFile;
  protected Map<String, String> tableMap = new HashMap<>();
  protected Map<String, String> TextToTableMap = new HashMap<>();

  public StepsBuilder(Editor editor, PsiFile psiFile) {
    this.editor = editor;
    this.psiFile = psiFile;
  }

  public Map<String, String> getTableMap() {
    return tableMap;
  }

  public Map<String, String> getTextToTableMap() {
    return TextToTableMap;
  }

  public List<PsiElement> build() {
    return null;
  }

  public static StepsBuilder getBuilder(Editor editor, PsiFile psiFile) {
    if (psiFile.getFileType().getClass().equals(ConceptFileType.class)) {
      return new ConceptStepsBuilder(editor, psiFile);
    }
    return new SpecStepsBuilder(editor, psiFile);
  }

  protected PsiElement getStep(PsiElement element, Class<?> stepClass) {
    if (element.getParent() == null) return null;
    if (element.getParent().getClass().equals(stepClass)) {
      return element.getParent();
    }
    return getStep(element.getParent(), stepClass);
  }

  protected List<PsiElement> getPsiElements(Class<?> stepClass) {
    SelectionModel selectionModel = editor.getSelectionModel();
    List<PsiElement> specSteps = new ArrayList<>();
    int currentOffset = selectionModel.getSelectionStart();
    while (selectionModel.getSelectionEnd() >= currentOffset) {
      try {
        if (psiFile.getText().charAt(currentOffset++) == '\n') continue;
        PsiElement step = getStep(psiFile.findElementAt(currentOffset), stepClass);
        if (step == null) return new ArrayList<>();
        specSteps.add(step);
        currentOffset += step.getText().length();
      }
      catch (Exception ex) {
        LOG.debug(ex);
      }
    }
    return specSteps;
  }
}
