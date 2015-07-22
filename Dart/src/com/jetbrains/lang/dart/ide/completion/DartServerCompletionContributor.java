package com.jetbrains.lang.dart.ide.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.icons.AllIcons;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.ui.RowIcon;
import com.intellij.util.PlatformIcons;
import com.intellij.util.ProcessingContext;
import com.jetbrains.lang.dart.DartLanguage;
import com.jetbrains.lang.dart.analyzer.DartAnalysisServerService;
import com.jetbrains.lang.dart.util.DartResolveUtil;
import org.apache.commons.lang3.StringUtils;
import org.dartlang.analysis.server.protocol.CompletionSuggestion;
import org.dartlang.analysis.server.protocol.Element;
import org.dartlang.analysis.server.protocol.ElementKind;
import org.dartlang.analysis.server.protocol.Location;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

import static com.intellij.patterns.PlatformPatterns.psiElement;

public class DartServerCompletionContributor extends CompletionContributor {
  public DartServerCompletionContributor() {
    extend(CompletionType.BASIC, psiElement().withLanguage(DartLanguage.INSTANCE), new CompletionProvider<CompletionParameters>() {
      @Override
      protected void addCompletions(@NotNull final CompletionParameters parameters,
                                    @NotNull final ProcessingContext context,
                                    @NotNull final CompletionResultSet result) {
        final VirtualFile file = DartResolveUtil.getRealVirtualFile(parameters.getOriginalFile());
        if (file == null) return;

        DartAnalysisServerService.getInstance().updateFilesContent();

        final String filePath = FileUtil.toSystemDependentName(file.getPath());
        final String completionId = DartAnalysisServerService.getInstance().completion_getSuggestions(filePath, parameters.getOffset());
        if (completionId == null) return;

        final Project project = parameters.getOriginalFile().getProject();
        DartAnalysisServerService.getInstance().addCompletions(completionId, new DartAnalysisServerService.CompletionSuggestionProcessor() {
          @Override
          public void process(CompletionSuggestion suggestion) {
            final LookupElement lookupElement = createLookupElement(project, suggestion);
            result.addElement(lookupElement);
          }
        });
      }
    });
  }

  private static LookupElement createLookupElement(Project project, CompletionSuggestion suggestion) {
    final Element element = suggestion.getElement();
    final Location location = element == null ? null : element.getLocation();
    final DartLookupObject lookupObject = new DartLookupObject(project, location, suggestion.getRelevance());

    LookupElementBuilder lookup = LookupElementBuilder.create(lookupObject, suggestion.getCompletion());

    if (element != null) {
      // @deprecated
      if (element.isDeprecated()) {
        lookup = lookup.withStrikeoutness(true);
      }
      // append type parameters
      final String typeParameters = element.getTypeParameters();
      if (typeParameters != null) {
        lookup = lookup.appendTailText(typeParameters, false);
      }
      // append parameters
      final String parameters = element.getParameters();
      if (parameters != null) {
        lookup = lookup.appendTailText(parameters, false);
      }
      // append return type
      final String returnType = element.getReturnType();
      if (!StringUtils.isEmpty(returnType)) {
        lookup = lookup.withTypeText(returnType, true);
      }
      // icon
      Icon icon = getBaseImage(element);
      if (icon != null) {
        icon = applyVisibility(icon, element.isPrivate());
        lookup = lookup.withIcon(icon);
      }
      // insert
      // TODO(scheglov) insert () with arguments
      //lookup = lookup.withInsertHandler(new InsertHandler<LookupElement>() {
      //  @Override
      //  public void handleInsert(InsertionContext context, LookupElement item) {
      //    context.getDocument().insertString(context.getStartOffset(), "()");
      //  }
      //});
    }

    // todo:
    //       quick doc preview (already works if corresponding PsiElement is correctly found by lookupObject)
    //       quick definition view (already works (but uses DartDocumentationProvider, not info from server) if corresponding PsiElement is correctly found by lookupObject)
    //       jump to item declaration (already works if corresponding PsiElement is correctly found by lookupObject)
    //       what to insert except identifier (e.g. parentheses, import directive, update show/hide, etc.)
    //       caret placement after insertion
    return lookup;
  }

  private static Icon applyVisibility(Icon base, boolean isPrivate) {
    RowIcon result = new RowIcon(2);
    result.setIcon(base, 0);
    Icon visibility = isPrivate ? PlatformIcons.PRIVATE_ICON : PlatformIcons.PUBLIC_ICON;
    result.setIcon(visibility, 1);
    return result;
  }

  private static Icon getBaseImage(Element element) {
    final String elementKind = element.getKind();
    if (elementKind.equals(ElementKind.CLASS) || elementKind.equals(ElementKind.CLASS_TYPE_ALIAS)) {
      if (element.isAbstract()) {
        return AllIcons.Nodes.AbstractClass;
      }
      return AllIcons.Nodes.Class;
    }
    else if (elementKind.equals(ElementKind.ENUM)) {
      return AllIcons.Nodes.Enum;
    }
    else if (elementKind.equals(ElementKind.ENUM_CONSTANT) || elementKind.equals(ElementKind.FIELD)) {
      return AllIcons.Nodes.Field;
    }
    else if (elementKind.equals(ElementKind.COMPILATION_UNIT)) {
      return PlatformIcons.FILE_ICON;
    }
    else if (elementKind.equals(ElementKind.CONSTRUCTOR)) {
      return AllIcons.Nodes.ClassInitializer;
    }
    else if (elementKind.equals(ElementKind.GETTER)) {
      return element.isTopLevelOrStatic() ? AllIcons.Nodes.PropertyReadStatic : AllIcons.Nodes.PropertyRead;
    }
    else if (elementKind.equals(ElementKind.SETTER)) {
      return element.isTopLevelOrStatic() ? AllIcons.Nodes.PropertyWriteStatic : AllIcons.Nodes.PropertyWrite;
    }
    else if (elementKind.equals(ElementKind.METHOD)) {
      if (element.isAbstract()) {
        return AllIcons.Nodes.AbstractMethod;
      }
      return AllIcons.Nodes.Method;
    }
    else if (elementKind.equals(ElementKind.FUNCTION)) {
      return AllIcons.Nodes.Function;
    }
    else if (elementKind.equals(ElementKind.FUNCTION_TYPE_ALIAS)) {
      return AllIcons.Nodes.Annotationtype;
    }
    else if (elementKind.equals(ElementKind.TOP_LEVEL_VARIABLE)) {
      return AllIcons.Nodes.Variable;
    }
    else {
      return null;
    }
  }
}
