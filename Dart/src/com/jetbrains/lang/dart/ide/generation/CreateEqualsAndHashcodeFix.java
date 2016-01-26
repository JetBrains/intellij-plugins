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
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartComponent;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Set;

public class CreateEqualsAndHashcodeFix extends BaseCreateMethodsFix<DartComponent> {
  public CreateEqualsAndHashcodeFix(DartClass dartClass) {
    super(dartClass);
  }

  @Override
  protected void processElements(@NotNull Project project, @NotNull Editor editor, Set<DartComponent> elementsToProcess) {
    final TemplateManager templateManager = TemplateManager.getInstance(project);
    anchor = doAddMethodsForOne(editor, templateManager, buildFunctionsText(templateManager, elementsToProcess), anchor);
  }

  @Override
  @NotNull
  protected String getNothingFoundMessage() {
    return ""; // can't be called actually because processElements() is overridden
  }

  protected Template buildFunctionsText(TemplateManager templateManager, Set<DartComponent> elementsToProcess) {
    final Template template = templateManager.createTemplate(getClass().getName(), DART_TEMPLATE_GROUP);
    template.setToReformat(true);


    final boolean doInsertOverrideAnnotation = CodeStyleSettingsManager.getSettings(myDartClass.getProject()).INSERT_OVERRIDE_ANNOTATION;
    if (doInsertOverrideAnnotation) {
      template.addTextSegment("@override\n");
    }
    template.addTextSegment("bool operator==(Object other) {\n");
    template.addTextSegment("if (identical(this, other)) {\nreturn true;\n}\n");
    template.addTextSegment("return other is " + myDartClass.getName());

    for (DartComponent component : elementsToProcess) {
      template.addTextSegment(" &&\n");
      template.addTextSegment("this." + component.getName() + " == other." + component.getName());
    }
    template.addTextSegment(";\n}\n");

    if (doInsertOverrideAnnotation) {
      template.addTextSegment("@override\n");
    }
    template.addTextSegment("int get hashCode {\n");
    if (elementsToProcess.isEmpty()) {
      template.addTextSegment("return 0;");
    }
    else {
      template.addTextSegment("return ");
      for (Iterator<DartComponent> iterator = elementsToProcess.iterator(); iterator.hasNext(); ) {
        DartComponent component = iterator.next();
        template.addTextSegment(component.getName() + ".hashCode");
        if (iterator.hasNext()) {
          template.addTextSegment(" ^");
        }
      }
      template.addTextSegment(";\n");
    }
    template.addTextSegment("}");
    template.addEndVariable();
    template.addTextSegment("\n");

    return template;
  }

  @Override
  protected Template buildFunctionsText(TemplateManager templateManager, DartComponent e) {
    // ignore
    return null;
  }
}
