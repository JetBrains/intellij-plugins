// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.jetbrains.lang.dart.ide.generation;

import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.jetbrains.lang.dart.DartBundle;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.ide.hierarchy.type.DartServerTypeHierarchyTreeStructure;
import com.jetbrains.lang.dart.psi.DartClass;
import com.jetbrains.lang.dart.psi.DartComponent;
import com.jetbrains.lang.dart.psi.DartComponentName;
import com.jetbrains.lang.dart.sdk.DartSdk;
import org.dartlang.analysis.server.protocol.TypeHierarchyItem;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Set;

public class CreateEqualsAndHashcodeFix extends BaseCreateMethodsFix<DartComponent> {

  private boolean mySuperclassOverridesEqualEqualAndHashCode;
  private boolean supportsObjectHashMethods = false;

  public CreateEqualsAndHashcodeFix(final @NotNull DartClass dartClass) {
    super(dartClass);
  }

  @Override
  public void beforeInvoke(@NotNull Project project, Editor editor, PsiElement file) {
    super.beforeInvoke(project, editor, file);
    mySuperclassOverridesEqualEqualAndHashCode = doesSuperclassOverrideEqualEqualAndHashCode(myDartClass);

    final DartSdk sdk = DartSdk.getDartSdk(project);
    supportsObjectHashMethods = sdk == null || StringUtil.compareVersionNumbers(sdk.getVersion(), "2.14") >= 0;
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
    return DartBundle.message("dart.generate.equalsAndHashcode");
  }

  @Override
  protected @NotNull String getNothingFoundMessage() {
    return ""; // can't be called actually because processElements() is overridden
  }

  private static boolean doesSuperclassOverrideEqualEqualAndHashCode(final @NotNull DartClass dartClass) {
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

    final int totalItems = elementsToProcess.size() + (mySuperclassOverridesEqualEqualAndHashCode ? 1 : 0);
    if (totalItems <= 0) {
      template.addTextSegment("0");
    }
    else if (supportsObjectHashMethods && totalItems > 1) {
      // hash() accepts up to 20 args, see https://api.flutter.dev/flutter/dart-core/Object/hash.html
      final boolean useHashAll = totalItems > 20;
      if (useHashAll) {
        template.addTextSegment("Object.hashAll(");
        template.addTextSegment("[");
      }
      else {
        template.addTextSegment("Object.hash(");
      }

      boolean shouldPrependComma = false;
      if (mySuperclassOverridesEqualEqualAndHashCode) {
        template.addTextSegment("super.hashCode");
        shouldPrependComma = true;
      }

      for (final DartComponent component : elementsToProcess) {
        if (shouldPrependComma) {
          template.addTextSegment(",");
        }
        template.addTextSegment(String.valueOf(component.getName()));
        shouldPrependComma = true;
      }

      if (useHashAll) {
        template.addTextSegment("]");
      }

      template.addTextSegment(")");
    }
    else {
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
