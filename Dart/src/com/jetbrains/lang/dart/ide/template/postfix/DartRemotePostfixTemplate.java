// Copyright 2000-2020 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.jetbrains.lang.dart.ide.template.postfix;

import com.intellij.CommonBundle;
import com.intellij.codeInsight.template.postfix.templates.PostfixTemplate;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.refactoring.util.CommonRefactoringUtil;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.assists.AssistUtils;
import com.jetbrains.lang.dart.assists.DartSourceEditException;
import org.dartlang.analysis.server.protocol.Position;
import org.dartlang.analysis.server.protocol.PostfixTemplateDescriptor;
import org.dartlang.analysis.server.protocol.SourceChange;
import org.jetbrains.annotations.NotNull;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Set;

public class DartRemotePostfixTemplate extends PostfixTemplate {

  protected DartRemotePostfixTemplate(PostfixTemplateDescriptor template) {
    this(template.getName(), template.getKey(), template.getExample());
  }

  protected DartRemotePostfixTemplate(@NotNull String name, @NotNull String key, @NotNull String example) {
    super(null, name, key, example, null);
  }

  public static DartRemotePostfixTemplate createTemplate(PostfixTemplateDescriptor template) {
    // TODO Automatically generate documentation subclasses from server data during build.
    try {
      String prefix = StringUtil.capitalize(template.getName());
      if (prefix.equals("!")) {
        prefix = "Bang";
      }
      Class<?> templateClass = Class.forName("com.jetbrains.lang.dart.ide.template.postfix." + prefix + "PostfixTemplate");
      Constructor<?> constructor = templateClass.getDeclaredConstructor(PostfixTemplateDescriptor.class);
      return (DartRemotePostfixTemplate)constructor.newInstance(template);
    }
    catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | InvocationTargetException | InstantiationException ex) {
      return new DartRemotePostfixTemplate(template);
    }
  }

  @Override
  public boolean isApplicable(@NotNull PsiElement context, @NotNull Document copyDocument, int newOffset) {
    final Project project = context.getProject();
    final PsiFile psiFile = context.getContainingFile();
    final DartAnalysisServerService service = DartAnalysisServerService.getInstance(project);
    String version = service.getSdkVersion();
    Set<PostfixTemplate> templates = DartPostfixTemplateProvider.getTemplates(version);
    if (templates == null) return false;

    boolean found = false;
    for (PostfixTemplate temp : templates) {
      if (temp.getKey().equals(getKey())) {
        // Ensure the requested template is defined by the analysis server currently in use.
        found = true;
        break;
      }
    }
    if (!found) {
      return false;
    }
    service.updateFilesContent(); // Ignore copyDocument
    return service.edit_isPostfixCompletionApplicable(psiFile.getOriginalFile().getVirtualFile(), newOffset, getKey());
  }

  @Override
  public void expand(@NotNull PsiElement context, @NotNull Editor editor) {
    final Project project = context.getProject();
    final PsiFile psiFile = context.getContainingFile();
    final int offset = editor.getCaretModel().getOffset();
    final DartAnalysisServerService service = DartAnalysisServerService.getInstance(project);
    service.updateFilesContent();
    final SourceChange sourceChange = service.edit_getPostfixCompletion(psiFile.getVirtualFile(), offset, getKey());
    if (sourceChange != null) {
      try {
        AssistUtils.applySourceChange(project, sourceChange, false);
        Position position = sourceChange.getSelection();
        if (position != null) {
          editor.getCaretModel().moveToOffset(service.getConvertedOffset(psiFile.getVirtualFile(), position.getOffset()));
        }
      }
      catch (DartSourceEditException e) {
        CommonRefactoringUtil.showErrorHint(project, editor, e.getMessage(), CommonBundle.getErrorTitle(), null);
      }
    }
  }

  @Override
  public boolean isEditable() {
    return false;
  }
}

class AssertPostfixTemplate extends DartRemotePostfixTemplate {
  AssertPostfixTemplate(PostfixTemplateDescriptor template) {
    super(template);
  }
}

class BangPostfixTemplate extends DartRemotePostfixTemplate {
  BangPostfixTemplate(PostfixTemplateDescriptor template) {
    super(template);
  }
}

class ForPostfixTemplate extends DartRemotePostfixTemplate {
  ForPostfixTemplate(PostfixTemplateDescriptor template) {
    super(template);
  }
}

class ForiPostfixTemplate extends DartRemotePostfixTemplate {
  ForiPostfixTemplate(PostfixTemplateDescriptor template) {
    super(template);
  }
}

class IterPostfixTemplate extends DartRemotePostfixTemplate {
  IterPostfixTemplate(PostfixTemplateDescriptor template) {
    super(template);
  }
}

class ElsePostfixTemplate extends DartRemotePostfixTemplate {
  ElsePostfixTemplate(PostfixTemplateDescriptor template) {
    super(template);
  }
}

class NotPostfixTemplate extends DartRemotePostfixTemplate {
  NotPostfixTemplate(PostfixTemplateDescriptor template) {
    super(template);
  }
}

class IfPostfixTemplate extends DartRemotePostfixTemplate {
  IfPostfixTemplate(PostfixTemplateDescriptor template) {
    super(template);
  }
}

class NnPostfixTemplate extends DartRemotePostfixTemplate {
  NnPostfixTemplate(PostfixTemplateDescriptor template) {
    super(template);
  }
}

class NotnullPostfixTemplate extends DartRemotePostfixTemplate {
  NotnullPostfixTemplate(PostfixTemplateDescriptor template) {
    super(template);
  }
}

class NullPostfixTemplate extends DartRemotePostfixTemplate {
  NullPostfixTemplate(PostfixTemplateDescriptor template) {
    super(template);
  }
}

class ParPostfixTemplate extends DartRemotePostfixTemplate {
  ParPostfixTemplate(PostfixTemplateDescriptor template) {
    super(template);
  }
}

class ReturnPostfixTemplate extends DartRemotePostfixTemplate {
  ReturnPostfixTemplate(PostfixTemplateDescriptor template) {
    super(template);
  }
}

class SwitchPostfixTemplate extends DartRemotePostfixTemplate {
  SwitchPostfixTemplate(PostfixTemplateDescriptor template) {
    super(template);
  }
}

class TryPostfixTemplate extends DartRemotePostfixTemplate {
  TryPostfixTemplate(PostfixTemplateDescriptor template) {
    super(template);
  }
}

class TryonPostfixTemplate extends DartRemotePostfixTemplate {
  TryonPostfixTemplate(PostfixTemplateDescriptor template) {
    super(template);
  }
}

class WhilePostfixTemplate extends DartRemotePostfixTemplate {
  WhilePostfixTemplate(PostfixTemplateDescriptor template) {
    super(template);
  }
}
