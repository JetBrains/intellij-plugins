// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.generation;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.DartTokenTypes;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartComponent;
import org.jetbrains.annotations.NotNull;

import java.util.Iterator;
import java.util.Set;

public class CreateToStringFix extends BaseCreateMethodsFix<DartComponent> {

  public CreateToStringFix(final @NotNull DartClass dartClass) {
    super(dartClass);
  }

  @Override
  protected void processElements(final @NotNull Project project,
                                 final @NotNull Editor editor,
                                 final @NotNull Set<DartComponent> elementsToProcess) {
    final TemplateManager templateManager = TemplateManager.getInstance(project);
    anchor = doAddMethodsForOne(editor, templateManager, buildFunctionsText(templateManager, elementsToProcess), anchor);
  }

  @Override
  protected @NotNull @NlsContexts.Command String getCommandName() {
    //noinspection DialogTitleCapitalization
    return DartBundle.message("dart.generate.toString");
  }

  @Override
  protected @NotNull String getNothingFoundMessage() {
    return ""; // can't be called actually because processElements() is overridden
  }

  protected Template buildFunctionsText(TemplateManager templateManager, Set<DartComponent> elementsToProcess) {
    final Template template = templateManager.createTemplate(getClass().getName(), DART_TEMPLATE_GROUP);
    template.setToReformat(true);

    template.addTextSegment("@override\n");
    template.addTextSegment("String toString() {");
    template.addTextSegment(DartTokenTypes.RETURN.toString());
    template.addTextSegment(" ");

    template.addTextSegment("'");
    //noinspection ConstantConditions
    template.addTextSegment(myDartClass.getName());
    template.addTextSegment("{");
    for (Iterator<DartComponent> iterator = elementsToProcess.iterator(); iterator.hasNext(); ) {
      DartComponent component = iterator.next();
      //noinspection ConstantConditions
      template.addTextSegment(component.getName());
      template.addTextSegment(": $");
      template.addTextSegment(component.getName());
      if (iterator.hasNext()) {
        template.addTextSegment(", ");
      }
    }
    template.addTextSegment("}';\n");
    template.addTextSegment("}");
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
