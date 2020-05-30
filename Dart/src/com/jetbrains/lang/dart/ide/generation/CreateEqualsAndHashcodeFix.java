// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.generation;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.ide.hierarchy.type.DartServerTypeHierarchyTreeStructure;
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

    for (DartClass superClass : DartServerTypeHierarchyTreeStructure.filterSuperClasses(project, items)) {
      if (superClass != null && superClass.getName() != null && !superClass.getName().equals("Object")) {
        if (DartGenerateEqualsAndHashcodeAction.doesClassContainEqualsAndHashCode(superClass)) {
          return true;
        }
      }
    }
    return false;
  }

  protected Template buildFunctionsText(TemplateManager templateManager, @NotNull Set<DartComponent> elementsToProcess) {
    final Template template = templateManager.createTemplate(getClass().getName(), DART_TEMPLATE_GROUP);
    template.setToReformat(true);

    template.addTextSegment("@override\n");
    template.addTextSegment("bool operator==(Object other) =>\nidentical(this, other) ||\n");
    if (mySuperclassOverridesEqualEqualAndHashCode) {
      template.addTextSegment("super == other && ");
    }
    template.addTextSegment("other is " + myDartClass.getName() + " && ");
    template.addTextSegment("runtimeType == other.runtimeType");
    for (DartComponent component : elementsToProcess) {
      template.addTextSegment(" && ");
      template.addTextSegment(component.getName() + " == other." + component.getName());
    }
    template.addTextSegment(";\n");

    template.addTextSegment("@override\n");
    template.addTextSegment("int get hashCode => ");
    boolean firstItem = true;
    if (mySuperclassOverridesEqualEqualAndHashCode) {
      template.addTextSegment("super.hashCode");
      firstItem = false;
    }
    for (DartComponent component : elementsToProcess) {
      if (!firstItem) {
        template.addTextSegment(" ^ ");
      }
      template.addTextSegment(component.getName() + ".hashCode");
      firstItem = false;
    }
    if (!mySuperclassOverridesEqualEqualAndHashCode && elementsToProcess.isEmpty()) {
      template.addTextSegment("0");
    }
    template.addTextSegment(";\n");
    template.addTextSegment(" "); // trailing space is removed when auto-reformatting, but it helps to enter line break if needed

    return template;
  }

  @Override
  protected Template buildFunctionsText(TemplateManager templateManager, DartComponent e) {
    // ignore
    return null;
  }
}
