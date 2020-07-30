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

package com.thoughtworks.gauge.extract;

import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogBuilder;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.util.containers.ContainerUtil;
import com.thoughtworks.gauge.GaugeBundle;
import com.thoughtworks.gauge.annotator.FileManager;
import com.thoughtworks.gauge.language.psi.ConceptTable;
import com.thoughtworks.gauge.language.psi.SpecPsiImplUtil;
import com.thoughtworks.gauge.language.psi.SpecTable;
import com.thoughtworks.gauge.language.psi.impl.ConceptStepImpl;
import com.thoughtworks.gauge.language.psi.impl.SpecStepImpl;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public final class ExtractConceptInfoCollector {
  public static final String CREATE_NEW_FILE = "Create New File(Enter info in the below field)";
  private final Editor editor;
  private final Map<String, String> tableMap;
  private final List<PsiElement> steps;
  private final Project project;

  public ExtractConceptInfoCollector(Editor editor, Map<String, String> tableMap, List<PsiElement> steps, Project project) {
    this.editor = editor;
    this.tableMap = tableMap;
    this.steps = steps;
    this.project = project;
  }

  public ExtractConceptInfo getAllInfo() {
    String steps = getFormattedSteps();
    List<String> args = getArgs(steps);
    final ExtractConceptDialog form = new ExtractConceptDialog(this.editor.getProject(), args);
    showDialog(steps, form);
    if (form.getInfo().cancelled) {
      return form.getInfo();
    }
    String fileName = form.getInfo().fileName;
    if (!fileName.startsWith(File.separator)) fileName = File.separator + fileName;
    return new ExtractConceptInfo(form.getInfo().conceptName, project.getBasePath() + fileName, form.getInfo().cancelled);
  }

  private List<String> getArgs(String steps) {
    List<String> args = new ArrayList<>();
    for (String step : steps.split("\n")) {
      args.addAll(ContainerUtil.map(SpecPsiImplUtil.getStepValueFor(this.steps.get(0), step, false).getParameters(),
                                    p -> getNameWithParamChar(StringUtil.unescapeStringCharacters(step), p)));
    }
    return args;
  }

  private static String getNameWithParamChar(String step, String p) {
    String arg = StringUtil.escapeStringCharacters(p);
    return step.charAt(step.indexOf(p) - 1) + arg + step.charAt(step.indexOf(p) + p.length());
  }

  private void showDialog(String steps, ExtractConceptDialog form) {
    final DialogBuilder builder = new DialogBuilder(editor.getProject());
    form.setData(steps, getConceptFileNames(), builder);
    builder.setCenterPanel(form.getRootPane());
    builder.setTitle(GaugeBundle.message("dialog.title.extract.concept"));
    builder.removeAllActions();
    builder.show();
  }

  private List<String> getConceptFileNames() {
    List<PsiFile> files = FileManager.getAllConceptFiles(editor.getProject());
    List<String> names = new ArrayList<>();
    names.add(CREATE_NEW_FILE);
    files.forEach((file) -> names.add(file.getVirtualFile().getPath().replace(project.getBasePath() + File.separator, "")));
    return names;
  }

  private String getFormattedSteps() {
    StringBuilder builder = new StringBuilder();
    for (PsiElement step : steps) {
      builder =
        step.getClass().equals(SpecStepImpl.class) ? formatStep(builder, (SpecStepImpl)step) : formatStep(builder, (ConceptStepImpl)step);
    }
    return builder.toString();
  }

  private StringBuilder formatStep(StringBuilder builder, SpecStepImpl step) {
    SpecTable table = step.getInlineTable();
    if (table != null) {
      builder.append(step.getText().trim().replace(table.getText().trim(), "").trim())
        .append(" <").append(tableMap.get(table.getText().trim())).append(">").append("\n");
      return builder;
    }
    return builder.append(step.getText().trim()).append("\n");
  }

  private StringBuilder formatStep(StringBuilder builder, ConceptStepImpl step) {
    ConceptTable table = step.getTable();
    if (table != null) {
      builder.append(step.getText().trim().replace(table.getText().trim(), "").trim())
        .append(" <").append(tableMap.get(table.getText().trim())).append(">").append("\n");
      return builder;
    }
    return builder.append(step.getText().trim()).append("\n");
  }
}