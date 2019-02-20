// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.angular2.codeInsight;

import com.intellij.codeInsight.completion.InsertHandler;
import com.intellij.codeInsight.completion.InsertionContext;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.application.WriteAction;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.SmartPointerManager;
import com.intellij.psi.SmartPsiElementPointer;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.ui.SimpleTextAttributes;
import one.util.streamex.StreamEx;
import org.angular2.entities.Angular2Declaration;
import org.angular2.entities.Angular2Module;
import org.angular2.inspections.quickfixes.Angular2FixesFactory;
import org.angular2.lang.Angular2Bundle;
import org.angular2.lang.expr.psi.Angular2TemplateBindings;
import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.List;

import static com.intellij.util.containers.ContainerUtil.find;
import static java.util.Collections.singletonList;
import static org.angular2.codeInsight.Angular2DeclarationsScope.DeclarationProximity;

public class Angular2CodeInsightUtils {

  @NotNull
  @Contract(pure = true)
  public static LookupElementBuilder decorateLookupElementWithModuleSource(@NotNull LookupElementBuilder element,
                                                                           @NotNull List<? extends Angular2Declaration> declarations,
                                                                           @NotNull DeclarationProximity proximity,
                                                                           @NotNull Angular2DeclarationsScope moduleScope) {
    if (proximity != DeclarationProximity.IN_SCOPE) {
      if (proximity == DeclarationProximity.EXPORTED_BY_PUBLIC_MODULE) {
        List<Angular2Module> modules = StreamEx.of(declarations)
          .flatCollection(declaration -> {
            List<Angular2Module> sources = moduleScope.getPublicModulesExporting(declaration);
            Angular2Module source = find(sources, module -> module.getDeclarations().contains(declaration));
            return source != null ? singletonList(source) : sources;
          })
          .distinct()
          .toList();
        if (modules.size() == 1) {
          element = element.appendTailText(" (" + modules.get(0).getName() + ")", true);
        }
        else if (!modules.isEmpty()) {
          element = element.appendTailText(
            " " + Angular2Bundle.message("angular.code-completion.modules.multiple-modules"),
            true);
        }
      }
      else if (proximity == DeclarationProximity.NOT_EXPORTED_BY_MODULE) {
        List<Angular2Module> modules = StreamEx.of(declarations)
          .flatCollection(Angular2Declaration::getAllModules)
          .distinct()
          .toList();
        if (modules.size() == 1) {
          element = element.appendTailText(
            " " + Angular2Bundle.message("angular.code-completion.modules.export-from", modules.get(0).getName()),
            true);
        }
        else if (!modules.isEmpty()) {
          element = element.appendTailText(
            " " + Angular2Bundle.message("angular.code-completion.modules.export-form-a-module"),
            true);
        }
        element = element.withItemTextForeground(SimpleTextAttributes.GRAYED_ATTRIBUTES.getFgColor());
      }
      else if (proximity == DeclarationProximity.NOT_DECLARED_IN_ANY_MODULE) {
        element = element.withItemTextForeground(SimpleTextAttributes.GRAYED_ATTRIBUTES.getFgColor())
          .appendTailText(Angular2Bundle.message("angular.code-completion.modules.declare-in-a-module"), true);
      }
    }
    return element;
  }

  public static LookupElementBuilder wrapWithImportDeclarationModuleHandler(@NotNull LookupElementBuilder element,
                                                                            @NotNull Class<? extends PsiElement> elementClass) {
    InsertHandler<LookupElement> originalHandler = element.getInsertHandler();
    return element.withInsertHandler(new InsertHandler<LookupElement>() {
      @Override
      public void handleInsert(@NotNull InsertionContext context, @NotNull LookupElement item) {
        boolean templateBindings = Angular2TemplateBindings.class == elementClass;
        PsiElement element = PsiTreeUtil.getParentOfType(context.getFile().findElementAt(context.getStartOffset()),
                                                         templateBindings ? XmlAttribute.class : elementClass);
        SmartPsiElementPointer<PsiElement> elementPointer = element != null ? SmartPointerManager.createPointer(element) : null;
        if (originalHandler != null) {
          originalHandler.handleInsert(context, item);
        }
        if (elementPointer == null) {
          return;
        }
        WriteAction.run(() -> PsiDocumentManager.getInstance(context.getProject()).commitDocument(context.getDocument()));
        element = elementPointer.getElement();
        if (element == null) {
          return;
        }
        if (templateBindings && element instanceof XmlAttribute) {
          element = Angular2TemplateBindings.get((XmlAttribute)element);
        }
        Angular2FixesFactory.ensureDeclarationResolved(element, context.getEditor(), true);
      }
    });
  }
}
