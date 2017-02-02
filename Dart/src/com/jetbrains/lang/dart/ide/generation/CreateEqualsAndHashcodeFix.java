/*
 * Copyright 2000-2016 JetBrains s.r.o.
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
package com.jetbrains.lang.dart.ide.generation;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.ide.hierarchy.DartHierarchyUtil;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartComponent;
import com.jetbrains.lang.dart.psi.DartComponentName;
import org.dartlang.analysis.server.protocol.TypeHierarchyItem;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public class CreateEqualsAndHashcodeFix extends BaseCreateMethodsFix<DartComponent> {

  private boolean mySuperclassOverridesEqualEqualAndHashCode;

  public CreateEqualsAndHashcodeFix(@NotNull final DartClass dartClass) {
    super(dartClass);
  }

  @Override
  public void beforeInvoke(@NotNull Project project, Editor editor, PsiElement file) {
    super.beforeInvoke(project, editor, file);
    mySuperclassOverridesEqualEqualAndHashCode = doesSuperclassOverrideEqualEqualAndHashCode(myDartClass);
  }

  @Override
  protected void processElements(@NotNull final Project project,
                                 @NotNull final Editor editor,
                                 @NotNull final Set<DartComponent> elementsToProcess) {
    final TemplateManager templateManager = TemplateManager.getInstance(project);
    anchor = doAddMethodsForOne(editor, templateManager, buildFunctionsText(templateManager, elementsToProcess), anchor);
  }

  @Override
  @NotNull
  protected String getNothingFoundMessage() {
    return ""; // can't be called actually because processElements() is overridden
  }

  private static boolean doesSuperclassOverrideEqualEqualAndHashCode(@NotNull final DartClass dartClass) {
    final Project project = dartClass.getProject();
    final VirtualFile file = dartClass.getContainingFile().getVirtualFile();
    final DartComponentName name = dartClass.getComponentName();
    if (name == null) {
      return false;
    }

    final List<TypeHierarchyItem> items = DartAnalysisServerService.getInstance(dartClass.getProject())
      .search_getTypeHierarchy(file, name.getTextRange().getStartOffset(), true);

    // The first item is the Dart class the query was run on, so skip it
    for (int i = 1; i < items.size(); i++) {
      final DartClass superDartClass = DartHierarchyUtil.findDartClass(project, items.get(i));
      if (superDartClass != null && superDartClass.getName() != null && !superDartClass.getName().equals("Object")) {
        if (DartGenerateEqualsAndHashcodeAction.doesClassContainEqualsAndHashCode(superDartClass)) {
          return true;
        }
      }
    }
    return false;
  }

  protected Template buildFunctionsText(TemplateManager templateManager, @NotNull Set<DartComponent> elementsToProcess) {
    final Template template = templateManager.createTemplate(getClass().getName(), DART_TEMPLATE_GROUP);
    template.setToReformat(true);

    final boolean doInsertOverrideAnnotation = CodeStyleSettingsManager.getSettings(myDartClass.getProject()).INSERT_OVERRIDE_ANNOTATION;
    if (doInsertOverrideAnnotation) {
      template.addTextSegment("@override\n");
    }
    template.addTextSegment("bool operator==(Object other) =>\nidentical(this, other) ||\n");
    if (mySuperclassOverridesEqualEqualAndHashCode) {
      template.addTextSegment("super == other &&\n");
    }
    template.addTextSegment("other is " + myDartClass.getName() + " &&\n");
    template.addTextSegment("runtimeType == other.runtimeType");
    for (DartComponent component : elementsToProcess) {
      template.addTextSegment(" &&\n");
      template.addTextSegment(component.getName() + " == other." + component.getName());
    }
    template.addTextSegment(";\n");

    if (doInsertOverrideAnnotation) {
      template.addTextSegment("@override\n");
    }
    template.addTextSegment("int get hashCode => ");
    boolean firstItem = true;
    if (mySuperclassOverridesEqualEqualAndHashCode) {
      template.addTextSegment("\nsuper.hashCode");
      firstItem = false;
    }
    for (DartComponent component : elementsToProcess) {
      if (!firstItem) {
        template.addTextSegment(" ^\n");
      }
      template.addTextSegment(component.getName() + ".hashCode");
      firstItem = false;
    }
    if (!mySuperclassOverridesEqualEqualAndHashCode && elementsToProcess.isEmpty()) {
      template.addTextSegment("0");
    }
    template.addTextSegment(";\n");
    template.addEndVariable();
    template.addTextSegment(" "); // trailing space is removed when auto-reformatting, but it helps to enter line break if needed

    return template;
  }

  @Override
  protected Template buildFunctionsText(TemplateManager templateManager, DartComponent e) {
    // ignore
    return null;
  }
}
