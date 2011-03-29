/*
 * @author max
 */
package com.intellij.lang.javascript.generation;

import com.intellij.codeInsight.hint.HintManager;
import com.intellij.featureStatistics.FeatureUsageTracker;
import com.intellij.ide.util.MemberChooser;
import com.intellij.lang.LanguageCodeInsightActionHandler;
import com.intellij.lang.javascript.JSBundle;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.JSNamedElement;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeListOwner;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.psi.resolve.ResolveProcessor;
import com.intellij.lang.javascript.validation.fixes.BaseCreateMethodsFix;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.command.CommandProcessor;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.ResolveState;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlFile;
import com.intellij.util.IncorrectOperationException;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import java.awt.*;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashMap;

public abstract class BaseJSGenerateHandler implements LanguageCodeInsightActionHandler {
  private boolean mySkipMemberChooserDialog = false;

  public void setSkipMemberChooserDialog(final boolean skipMemberChooserDialog) {
    mySkipMemberChooserDialog = skipMemberChooserDialog;
  }

  protected @Nullable String getProductivityFeatureId() {
    return null;
  }

  public void invoke(@NotNull final Project project, @NotNull final Editor editor, @NotNull final PsiFile file) {
    final JSClass jsClass = findClass(file, editor);
    if (jsClass == null) return;

    final Collection<JSNamedElementNode> selectedElements;

    if (collectCandidatesAndShowDialog()) {
      final Collection<JSNamedElementNode> candidates = new ArrayList<JSNamedElementNode>();
      collectCandidates(jsClass, candidates);

      if (candidates.isEmpty()) {
        if (canHaveEmptySelectedElements()) {
          selectedElements = Collections.emptyList();
        }
        else {
          if (!ApplicationManager.getApplication().isHeadlessEnvironment()) {
            HintManager.getInstance().showErrorHint(editor, getNoCandidatesMessage());
          }
          return;
        }
      }
      else {
        if (mySkipMemberChooserDialog || ApplicationManager.getApplication().isUnitTestMode()) {
          selectedElements = candidates;
        }
        else {
          final MemberChooser<JSNamedElementNode> chooser =
            createMemberChooserDialog(project, jsClass, candidates, canHaveEmptySelectedElements(), true, JSBundle.message(getTitleKey()));
          chooser.show();
          if (chooser.getExitCode() != DialogWrapper.OK_EXIT_CODE) return;

          final Collection<JSNamedElementNode> selected = chooser.getSelectedElements();
          if (selected == null) {
            selectedElements = Collections.emptyList();
          }
          else {
            selectedElements = selected;
          }
        }
      }
    }
    else {
      selectedElements = Collections.emptyList();
    }

    final String featureId = getProductivityFeatureId();
    if (featureId != null) FeatureUsageTracker.getInstance().triggerFeatureUsed(featureId);
    final BaseCreateMethodsFix createMethodsFix = createFix(jsClass);
    doInvoke(project, editor, file, selectedElements, createMethodsFix);
  }

  protected void doInvoke(final Project project,
                     final Editor editor,
                     final PsiFile file,
                     final Collection<JSNamedElementNode> selectedElements,
                     final BaseCreateMethodsFix createMethodsFix) {
    Runnable runnable = new Runnable() {
      public void run() {
        createMethodsFix.addElementsToProcessFrom(selectedElements);
        createMethodsFix.beforeInvoke(project, editor, file);

        ApplicationManager.getApplication().runWriteAction(new Runnable() {
          public void run() {
            try {
              createMethodsFix.invoke(project, editor, file);
            }
            catch (IncorrectOperationException ex) {
              Logger.getInstance(getClass().getName()).error(ex);
            }
          }
        });
      }
    };

    if (CommandProcessor.getInstance().getCurrentCommand() == null) {
      CommandProcessor.getInstance().executeCommand(project, runnable, getClass().getName(), null);
    } else {
      runnable.run();
    }
  }

  protected MemberChooser<JSNamedElementNode> createMemberChooserDialog(final Project project,
                                                                      final JSClass jsClass,
                                                                      final Collection<JSNamedElementNode> candidates,
                                                                      final boolean allowEmptySelection,
                                                                      final boolean allowMultipleSelection,
                                                                      String title) {
    final MemberChooser<JSNamedElementNode> chooser =
      new MemberChooser<JSNamedElementNode>(candidates.toArray(new JSNamedElementNode[candidates.size()]), allowEmptySelection,
                                            allowMultipleSelection, project, false) {
        protected void init() {
          super.init();
          if (!allowEmptySelection) {
            myTree.addTreeSelectionListener(new TreeSelectionListener() {
              public void valueChanged(final TreeSelectionEvent e) {
                setOKActionEnabled(myTree.getSelectionCount() > 0);
              }
            });
          }
          else {
            setOKActionEnabled(true);
          }
        }

        protected JComponent createCenterPanel() {
          final JComponent superComponent = super.createCenterPanel();
          final JComponent optionsComponent = getOptionsComponent(jsClass, candidates);
          if (optionsComponent == null) {
            return superComponent;
          }
          else {
            final JPanel panel = new JPanel(new BorderLayout());
            panel.add(superComponent, BorderLayout.CENTER);
            panel.add(optionsComponent, BorderLayout.SOUTH);
            return panel;
          }
        }
      };

    chooser.setTitle(title);
    chooser.setCopyJavadocVisible(false);
    return chooser;
  }

  @Nullable
  protected JComponent getOptionsComponent(JSClass jsClass, final Collection<JSNamedElementNode> candidates){
    return null;
  }

  protected boolean canHaveEmptySelectedElements() {
    return false;
  }

  @Nullable
  public static JSClass findClass(PsiFile file, Editor editor) {
    if (file instanceof XmlFile && JavaScriptSupportLoader.isFlexMxmFile(file)) {
      return XmlBackedJSClassImpl.getXmlBackedClass((XmlFile)file);
    }

    if (!(file instanceof JSFile)) return null;

    final PsiElement at = file.findElementAt(editor.getCaretModel().getOffset());
    if (at == null) return null;

    JSClass clazz = PsiTreeUtil.getParentOfType(at, JSClass.class);
    if (clazz == null) {
      final PsiFile containingFile = at.getContainingFile();
      final PsiElement element = JSResolveUtil.getClassReferenceForXmlFromContext(containingFile);
      if (element instanceof JSClass) clazz = (JSClass)element;
    } else if (JSResolveUtil.isArtificialClassUsedForReferenceList(clazz)) {
      final PsiElement context = clazz.getContainingFile().getContext();
      if (context != null && context.getContainingFile() instanceof XmlFile) {
        clazz = XmlBackedJSClassImpl.getXmlBackedClass((XmlFile)context.getContainingFile());
      }
      else {
        clazz = null;
      }
    }

    return clazz;
  }

  protected abstract /*@PropertyKey(resourceBundle = JSBundle.BUNDLE)*/ @NonNls String getTitleKey();

  protected String getNoCandidatesMessage() {
    return JSBundle.message("no.candidates");
  }

  protected abstract BaseCreateMethodsFix createFix(JSClass clazz);

  protected boolean collectCandidatesAndShowDialog(){
    return true;
  }

  protected void collectCandidates(final JSClass clazz, final Collection<JSNamedElementNode> candidates) {
  }

  protected static void collectJSVariables(final JSClass clazz,
                                           final Collection<JSNamedElementNode> candidates,
                                           final boolean skipThatHaveGetters,
                                           final boolean skipThatHaveSetters,
                                           final boolean skipStatics
  ) {
    final LinkedHashMap<String, JSNamedElement> candidatesMap = new LinkedHashMap<String, JSNamedElement>();
    final JSCodeStyleSettings codeStyleSettings =
      CodeStyleSettingsManager.getSettings(clazz.getProject()).getCustomSettings(JSCodeStyleSettings.class);
    final ResolveProcessor processor = new ResolveProcessor(null) {
      {
        setToProcessMembers(true);
        setToProcessHierarchy(false);
        setLocalResolve(true);
      }

      public boolean execute(final PsiElement element, final ResolveState state) {
        final JSNamedElement namedElement = (JSNamedElement)element;
        if (skipStatics && element instanceof JSAttributeListOwner) {
          JSAttributeList attributeList = ((JSAttributeListOwner)element).getAttributeList();
          if (attributeList != null && attributeList.hasModifier(JSAttributeList.ModifierType.STATIC)) return true;
        }
        if (!(element instanceof JSVariable)) {
          if (element instanceof JSFunction) {
            final JSFunction function = (JSFunction)element;
            if (skipThatHaveGetters && function.isGetProperty() || skipThatHaveSetters && function.isSetProperty()) {
              candidatesMap.put(function.getName(), function);
            }
          }
          return true;
        }
        else if (((JSVariable)element).isConst()) {
          return true;
        }

        final String name = namedElement.getName();
        final String accessorName = JSResolveUtil.transformVarNameToAccessorName(name, codeStyleSettings);
        if (/*!name.equals(accessorName) &&*/ !candidatesMap.containsKey(accessorName)) candidatesMap.put(accessorName, namedElement);
        return true;
      }
    };

    clazz.processDeclarations(processor, ResolveState.initial(), clazz, clazz);
    for (JSNamedElement n : candidatesMap.values()) {
      if (n instanceof JSVariable) {
        candidates.add(new JSNamedElementNode(n));
      }
    }
  }

  public boolean startInWriteAction() {
    return false;
  }

  public boolean isValidFor(final Editor editor, final PsiFile file) {
    final JSClass jsClass = findClass(file, editor);
    return jsClass != null && !jsClass.isInterface();
  }
}
