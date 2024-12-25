// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.lang.javascript.refactoring.ui;

import com.intellij.lang.javascript.JavaScriptBundle;
import com.intellij.lang.javascript.dialects.JSDialectSpecificHandlersFactory;
import com.intellij.lang.javascript.flex.FlexSupportLoader;
import com.intellij.lang.javascript.parsing.JavaScriptParserBase;
import com.intellij.lang.javascript.psi.JSElementFactory;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.psi.resolve.ResolveProcessor;
import com.intellij.lang.javascript.ui.JSClassChooserDialog;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.event.DocumentListener;
import com.intellij.openapi.editor.ex.EditorEx;
import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.ComponentWithBrowseButton;
import com.intellij.openapi.ui.Messages;
import com.intellij.openapi.util.Computable;
import com.intellij.openapi.util.Condition;
import com.intellij.openapi.util.NlsContexts;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiCodeFragment;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.ui.EditorComboBox;
import com.intellij.ui.EditorTextField;
import com.intellij.ui.RecentsManager;
import com.intellij.ui.TextAccessor;
import com.intellij.util.ArrayUtilRt;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.List;

public abstract class JSReferenceEditor extends ComponentWithBrowseButton<JComponent> implements TextAccessor {

  private final Project myProject;
  private final String myRecentsKey;
  private GlobalSearchScope myScope;
  private @NlsContexts.DialogMessage @Nullable String myChooserBlockMessage;

  protected abstract ActionListener createActionListener();

  protected JSReferenceEditor(final String text,
                              @NotNull Project project,
                              final @Nullable String recentsKey,
                              final GlobalSearchScope scope,
                              final @Nullable JavaScriptParserBase.ForceContext typeContext,
                              @Nullable Condition<JSClass> filter,
                              @NlsContexts.DialogTitle @NotNull String chooserTitle,
                              boolean needPackages,
                              @Nullable PsiElement context) {
    super(recentsKey != null ?
          new JSEditorComboBox(createDocument(StringUtil.notNullize(text), project, scope, typeContext, filter, needPackages, context),
                               project) :
          new JSEditorTextField(project,
                                createDocument(StringUtil.notNullize(text), project, scope, typeContext, filter, needPackages, context)),
          null);
    myProject = project;
    myRecentsKey = recentsKey;
    myScope = scope;
    if (myRecentsKey != null) {
      final List<String> recentEntries = RecentsManager.getInstance(myProject).getRecentEntries(recentsKey);
      if (recentEntries != null) {
        ((EditorComboBox)getChildComponent()).setHistory(ArrayUtilRt.toStringArray(recentEntries));
      }
      if (text != null) {
        ((EditorComboBox)getChildComponent()).prependItem(text);
      }
    }

    final ActionListener actionListener = createActionListener();
    addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if (myChooserBlockMessage != null) {
          Messages.showErrorDialog(myChooserBlockMessage, chooserTitle);
        }
        else {
          actionListener.actionPerformed(e);
        }
      }
    });
  }

  public void setScope(GlobalSearchScope scope) {
    myScope = scope;
    PsiFile codeFragment = PsiDocumentManager.getInstance(myProject).getPsiFile(getDocument());
    ((PsiCodeFragment)codeFragment).forceResolveScope(scope);
    invalidateHighlight();
  }

  public void invalidateHighlight() {
    setText(getText(), true);
  }

  public void setChooserBlockingMessage(@NlsContexts.DialogMessage @Nullable String message) {
    myChooserBlockMessage = message;
  }

  /**
   * @deprecated remove this hacky way when editor allows to override it's preferred height
   */
  @Deprecated
  public void setHeightProvider(Computable<Integer> heightProvider) {
    if (getChildComponent() instanceof JSEditorComboBox) {
      ((JSEditorComboBox)getChildComponent()).setHeightProvider(heightProvider);
    }
  }

  private static Document createDocument(final String text,
                                         Project project,
                                         GlobalSearchScope scope,
                                         @Nullable JavaScriptParserBase.ForceContext typeContext,
                                         final @Nullable Condition<JSClass> filter,
                                         final boolean needPackages,
                                         @Nullable PsiElement context) {
    final JSFile fragment =
      JSElementFactory.createExpressionCodeFragment(project, text, null, FlexSupportLoader.ECMA_SCRIPT_L4, scope, JSElementFactory.TopLevelCompletion.NO, typeContext);
    fragment.putUserData(JSResolveUtil.contextKey, context);
    fragment.putUserData(ResolveProcessor.PROCESSING_OPTIONS, new ResolveProcessor.ProcessingOptions() {
      @Override
      public boolean needPackages() {
        return needPackages;
      }

      @Override
      public @Nullable Condition<JSClass> getFilter() {
        return filter;
      }
    });
    return PsiDocumentManager.getInstance(project).getDocument(fragment);
  }

  public void addDocumentListener(DocumentListener listener) {
    getDocument().addDocumentListener(listener);
  }

  // used in tests
  public PsiFile getPsiFile() {
    return PsiDocumentManager.getInstance(myProject).getPsiFile(getDocument());
  }

  private Document getDocument() {
    return getChildComponent() instanceof EditorComboBox
           ? ((EditorComboBox)getChildComponent()).getDocument() :
           ((EditorTextField)getChildComponent()).getDocument();
  }

  @Override
  public String getText() {
    return getDocument().getText().trim();
  }

  @Override
  public void setText(final String text) {
    setText(text, false);
  }

  private void setText(String text, boolean keepCaret) {
    if (text == null) {
      text = "";
    }
    if (getChildComponent() instanceof EditorComboBox) {
      ((EditorComboBox)getChildComponent()).setText(text);
      if (!keepCaret && StringUtil.isNotEmpty(text)) {
        final EditorEx editor = ((EditorComboBox)getChildComponent()).getEditorEx();
        if (editor != null) {
          editor.getCaretModel().moveToOffset(text.length());
        }
      }
    }
    else {
      ((EditorTextField)getChildComponent()).setText(text);
      if (!keepCaret && StringUtil.isNotEmpty(text)) {
        final Editor editor = ((EditorTextField)getChildComponent()).getEditor();
        if (editor != null) {
          editor.getCaretModel().moveToOffset(text.length());
        }
      }
    }
  }

  public void updateRecents() {
    if (myRecentsKey != null) {
      RecentsManager.getInstance(myProject).registerRecentEntry(myRecentsKey, getText());
    }
  }

  protected GlobalSearchScope getScope() {
    return myScope;
  }

  public static JSReferenceEditor forClassName(final String text,
                                               final @NotNull Project project,
                                               final @Nullable String recentsKey,
                                               GlobalSearchScope scope,
                                               final @Nullable JavaScriptParserBase.ForceContext typeContext,
                                               final @Nullable Condition<JSClass> classFilter,
                                               final @NlsContexts.DialogTitle @NotNull String chooserTitle) {
    return forClassName(text, project, recentsKey, scope, typeContext, classFilter, chooserTitle, null);
  }

  public static JSReferenceEditor forClassName(final String text,
                                               final @NotNull Project project,
                                               final @Nullable String recentsKey,
                                               GlobalSearchScope scope,
                                               final @Nullable JavaScriptParserBase.ForceContext typeContext,
                                               final @Nullable Condition<JSClass> classFilter,
                                               @NlsContexts.DialogTitle @NotNull String chooserTitle,
                                               @Nullable PsiElement context) {
    return new JSReferenceEditor(text, project, recentsKey, scope, typeContext, classFilter,
                                 chooserTitle, false, context) {
      @Override
      protected ActionListener createActionListener() {
        return new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            if (DumbService.getInstance(project).isDumb()) {
              Messages.showWarningDialog(JavaScriptBundle.message("class.chooser.not.available.in.dumb.mode"), chooserTitle);
              return;
            }
            PsiElement initialClass =
              getText() != null ? JSDialectSpecificHandlersFactory.forLanguage(FlexSupportLoader.ECMA_SCRIPT_L4).getClassResolver()
                .findClassByQName(getText(), getScope()) : null;
            JSClassChooserDialog chooser =
              new JSClassChooserDialog(project, chooserTitle, getScope(), initialClass instanceof JSClass ? (JSClass)initialClass : null,
                                       classFilter);
            if (chooser.showDialog()) {
              JSClass clazz = chooser.getSelectedClass();
              if (clazz != null) {
                setText(clazz.getQualifiedName());
              }
            }
          }
        };
      }
    };
  }
}
