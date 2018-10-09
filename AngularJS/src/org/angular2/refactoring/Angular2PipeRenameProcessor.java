// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.refactoring;

import com.intellij.lang.javascript.psi.JSCallExpression;
import com.intellij.lang.javascript.psi.JSLiteralExpression;
import com.intellij.lang.javascript.psi.JSProperty;
import com.intellij.lang.javascript.psi.ecma6.TypeScriptClass;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.lang.javascript.refactoring.JSDefaultRenameProcessor;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.*;
import com.intellij.refactoring.listeners.RefactoringElementListener;
import com.intellij.refactoring.rename.RenameDialog;
import com.intellij.refactoring.rename.RenameUtil;
import com.intellij.usageView.UsageInfo;
import com.intellij.usageView.UsageViewTypeLocation;
import com.intellij.util.IncorrectOperationException;
import org.angular2.Angular2DecoratorUtil;
import org.angular2.codeInsight.Angular2PipeUtil;
import org.angular2.entities.Angular2EntitiesProvider;
import org.angular2.entities.Angular2Pipe;
import org.angular2.lang.Angular2LangUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Map;
import java.util.Objects;

import static org.angular2.codeInsight.Angular2PipeUtil.NAME_PROP;
import static org.angular2.codeInsight.Angular2PipeUtil.PIPE_DEC;

public class Angular2PipeRenameProcessor extends JSDefaultRenameProcessor {

  @Override
  public boolean canProcessElement(@NotNull PsiElement element) {
    return Angular2PipeUtil.getPipe(element) != null
           && Angular2LangUtil.isAngular2Context(element);
  }

  @Override
  public boolean isInplaceRenameSupported() {
    return false;
  }

  @Nullable
  @Override
  public PsiElement substituteElementToRename(@NotNull PsiElement element, @Nullable Editor editor) {
    return Angular2PipeUtil.getPipe(element);
  }

  @Override
  public void renameElement(@NotNull PsiElement element,
                            @NotNull String newName,
                            @NotNull UsageInfo[] usages,
                            @Nullable RefactoringElementListener listener) throws IncorrectOperationException {
    if (element instanceof JSImplicitElement && element.getParent() instanceof TypeScriptClass) {
      JSCallExpression decorator = Angular2DecoratorUtil.getDecorator((TypeScriptClass)element.getParent(), PIPE_DEC);
      JSProperty property = Angular2DecoratorUtil.getProperty(decorator, NAME_PROP);
      if (property != null && property.getValue() instanceof JSLiteralExpression) {
        PsiReference[] refs = property.getValue().getReferences();
        for (PsiReference ref : refs) {
          if (ref.resolve() == element) {
            ref.handleElementRename(newName);
          }
        }
      }
    }
    RenameUtil.doRenameGenericNamedElement(element, newName, usages, listener);
  }

  @NotNull
  @Override
  public RenameDialog createRenameDialog(@NotNull Project project,
                                         @NotNull final PsiElement element,
                                         PsiElement nameSuggestionContext,
                                         Editor editor) {
    return super.createRenameDialog(project, Objects.requireNonNull(Angular2PipeUtil.getPipe(element)),
                                    nameSuggestionContext, editor);
  }

  @Override
  public void prepareRenaming(@NotNull PsiElement element, @NotNull String newName, @NotNull Map<PsiElement, String> allRenames) {
    assert element instanceof JSImplicitElement;
    Angular2Pipe metadata = Angular2EntitiesProvider.getPipe((JSImplicitElement)element);
    if (metadata.getTypeScriptClass() != null) {
      JSClass pipeClass = metadata.getTypeScriptClass();
      allRenames.put(pipeClass, getDefaultPipeClassName(newName));
      if (pipeClass.getContainingFile().getName().equals(getDefaultPipeFileName(((JSImplicitElement)element).getName()))) {
        allRenames.put(pipeClass.getContainingFile(), getDefaultPipeFileName(newName));
        VirtualFile specFile = pipeClass.getContainingFile().getVirtualFile()
          .getParent().findFileByRelativePath(getDefaultPipeSpecFileName(((JSImplicitElement)element).getName()));
        if (specFile != null) {
          PsiFile specPsiFile = pipeClass.getManager().findFile(specFile);
          if (specPsiFile != null) {
            allRenames.put(specPsiFile, getDefaultPipeSpecFileName(newName));
          }
        }
      }
    }
  }

  private static String getDefaultPipeFileName(String pipeName) {
    return pipeName + ".pipe.ts";
  }

  private static String getDefaultPipeSpecFileName(String pipeName) {
    return pipeName + ".pipe.spec.ts";
  }

  private static String getDefaultPipeClassName(String pipeName) {
    return StringUtil.capitalize(pipeName) + "Pipe";
  }

  public static class Angular2PipeElementDescriptor implements ElementDescriptionProvider {
    @Nullable
    @Override
    public String getElementDescription(@NotNull PsiElement element, @NotNull ElementDescriptionLocation location) {
      if (element instanceof JSImplicitElement
          && Angular2PipeUtil.isPipeType(((JSImplicitElement)element).getTypeString())) {
        if (location instanceof UsageViewTypeLocation) return "pipe";
        return ((JSImplicitElement)element).getName();
      }
      return null;
    }
  }
}
