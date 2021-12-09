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

package com.thoughtworks.gauge.findUsages;

import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiManager;
import com.intellij.util.indexing.FileBasedIndex;
import com.thoughtworks.gauge.annotator.FileManager;
import com.thoughtworks.gauge.language.psi.impl.ConceptStepImpl;
import com.thoughtworks.gauge.language.psi.impl.SpecStepImpl;
import com.thoughtworks.gauge.stub.GaugeFileStubIndex;

import java.util.*;
import java.util.stream.Stream;

public final class StepCollector {
  private final Project project;
  private final HashMap<String, List<PsiElement>> stepTextToElement;

  public StepCollector(Project project) {
    this.project = project;
    stepTextToElement = new HashMap<>();
  }

  public void collect() {
    List<VirtualFile> conceptFiles = FileManager.getConceptFiles(project);
    List<VirtualFile> specFiles = FileManager.getAllSpecFiles(project);

    Stream.concat(conceptFiles.stream(), specFiles.stream()).forEach(f -> {
      Collection<Integer> values =
        Objects.requireNonNullElse(FileBasedIndex.getInstance().getSingleEntryIndexData(GaugeFileStubIndex.NAME, f, project), Collections.emptyList());
      if (values.size() > 0) {
        getSteps(PsiManager.getInstance(project).findFile(f), values);
      }
    });
  }

  public List<PsiElement> get(String stepText) {
    return stepTextToElement.get(stepText) == null ? new ArrayList<>() : stepTextToElement.get(stepText);
  }

  private void getSteps(PsiFile psiFile, Collection<Integer> offsets) {
    for (Integer offset : offsets) {
      PsiElement stepElement = getStepElement(psiFile.findElementAt(offset));
      if (stepElement == null) continue;
      if (stepElement.getClass().equals(SpecStepImpl.class)) {
        addElement(stepElement, cleanText(((SpecStepImpl)stepElement).getStepValue().getStepText()));
      }
      else {
        addElement(stepElement, cleanText(((ConceptStepImpl)stepElement).getStepValue().getStepText()));
      }
    }
  }

  private static String cleanText(String text) {
    if (text == null || text.isEmpty()) return "";
    return text.charAt(0) == '*' || text.charAt(0) == '#' ? text.substring(1).trim() : text.trim();
  }

  private void addElement(PsiElement stepElement, String stepText) {
    List<PsiElement> elementsList = stepTextToElement.get(stepText);
    if (elementsList == null) {
      List<PsiElement> elements = new ArrayList<>();
      elements.add(stepElement);
      stepTextToElement.put(stepText, elements);
      return;
    }
    elementsList.add(stepElement);
  }

  private static PsiElement getStepElement(PsiElement selectedElement) {
    if (selectedElement == null) return null;
    if (selectedElement.getClass().equals(SpecStepImpl.class) || selectedElement.getClass().equals(ConceptStepImpl.class)) {
      return selectedElement;
    }
    return getStepElement(selectedElement.getParent());
  }
}