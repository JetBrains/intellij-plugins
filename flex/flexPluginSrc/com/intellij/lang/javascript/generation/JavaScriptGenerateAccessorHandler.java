package com.intellij.lang.javascript.generation;

import com.intellij.codeInsight.template.Template;
import com.intellij.lang.javascript.JSBundle;
import com.intellij.lang.javascript.flex.ImportUtils;
import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.formatter.JSCodeStyleSettings;
import com.intellij.lang.javascript.index.JSTypeEvaluateManager;
import com.intellij.lang.javascript.psi.JSFunction;
import com.intellij.lang.javascript.psi.JSParameterList;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.lang.javascript.psi.JSVariable;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.refactoring.JSVisibilityUtil;
import com.intellij.lang.javascript.validation.fixes.BaseCreateMethodsFix;
import com.intellij.lang.javascript.validation.fixes.CreateJSVariableIntentionAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.Ref;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.codeStyle.CodeStyleSettingsManager;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.PsiElementProcessor;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.refactoring.RefactoringFactory;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.containers.MultiMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.awt.*;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Collection;

/**
 * @author Maxim.Mossienko
 *         Date: Jul 19, 2008
 *         Time: 1:11:10 AM
 */
public class JavaScriptGenerateAccessorHandler extends BaseJSGenerateHandler {

  public static enum GenerationMode {
    Getter, Setter, GetterAndSetter
  }

  private final GenerationMode myMode;

  private JCheckBox myMakePrivate;
  private BindablePropertiesForm myBindablePropertiesForm;

  public JavaScriptGenerateAccessorHandler(final GenerationMode mode) {
    myMode = mode;
  }

  protected String getTitleKey() {
    return myMode == GenerationMode.Getter
           ? "generate.getter.fields.chooser.title"
           : myMode == GenerationMode.Setter
             ? "generate.setter.fields.chooser.title"
             : "generate.getter.setter.chooser.title";
  }

  protected String getNoCandidatesMessage() {
    return myMode == GenerationMode.Getter
           ? JSBundle.message("no.variables.for.getter")
           : myMode == GenerationMode.Setter
             ? JSBundle.message("no.variables.for.setter")
             : JSBundle.message("no.variables.for.getter.setter");
  }

  @Nullable
  protected JComponent getOptionsComponent(final JSClass jsClass, final Collection<JSNamedElementNode> candidates) {
    if (!ApplicationManager.getApplication().isUnitTestMode()) {

      for (final JSNamedElementNode candidate : candidates) {
        final PsiElement element = candidate.getPsiElement();
        if (element instanceof JSVariable){
          final JSAttributeList attrList = ((JSVariable)element).getAttributeList();
          if (attrList == null || attrList.getAccessType() != JSAttributeList.AccessType.PRIVATE) {
            myMakePrivate = new JCheckBox("Make private", true);
            myMakePrivate.setFocusable(false);
            myMakePrivate.setMnemonic('M');
            break;
          }
        }
      }


      if (isEventDispatcher(jsClass)) {
        myBindablePropertiesForm =
          new BindablePropertiesForm(jsClass.getProject(), myMode == GenerationMode.Setter || myMode == GenerationMode.GetterAndSetter);
        if (myMakePrivate != null) {
          JPanel panel = new JPanel(new BorderLayout());
          panel.add(myBindablePropertiesForm.getMainPanel(), BorderLayout.CENTER);
          panel.add(myMakePrivate, BorderLayout.SOUTH);
          return panel;
        }
        else {
          return myBindablePropertiesForm.getMainPanel();
        }
      }
    }

    return myMakePrivate;
  }

  public static boolean isEventDispatcher(final JSClass jsClass) {
    final PsiElement eventClass = JSResolveUtil.unwrapProxy(JSResolveUtil.findClassByQName("flash.events.IEventDispatcher", jsClass));
    if (!(eventClass instanceof JSClass)) return false;
    if (JSResolveUtil.checkClassHasParentOfAnotherOne(jsClass, (JSClass)eventClass, null)) {
      return true;
    }
    return false;
  }

  protected BaseCreateMethodsFix createFix(final JSClass jsClass) {
    final EventBinder eventBinder;
    if (ApplicationManager.getApplication().isUnitTestMode()) {
      final boolean isEventDispatcher = isEventDispatcher(jsClass);

      eventBinder = new EventBinder() {
        public boolean isBindEvent() {
          return isEventDispatcher;
        }

        public String getEventName(final String parameterName) {
          return JSResolveUtil.transformVarNameToAccessorName(parameterName, jsClass.getProject()) + "Changed";
        }

        public boolean isCreateEventConstant() {
          return isEventDispatcher;
        }

        public String getEventConstantName(final String parameterName) {
          return JSResolveUtil.transformVarNameToAccessorName(parameterName, jsClass.getProject()).toUpperCase() + "_CHANGED_EVENT";
        }
      };
    }
    else {
      eventBinder = myBindablePropertiesForm;
    }

    final boolean makePrivate = myMakePrivate != null ? myMakePrivate.isSelected():true;

    if (myMode == GenerationMode.GetterAndSetter) {
      return new BaseCreateMethodsFix<JSVariable>(jsClass) {
        final MyBaseCreateMethodsFix generateGetterFix =
          new MyBaseCreateMethodsFix(GenerationMode.Getter, jsClass, eventBinder, makePrivate, null);
        final MyBaseCreateMethodsFix generateSetterFix =
          new MyBaseCreateMethodsFix(GenerationMode.Setter, jsClass, eventBinder, makePrivate, null);

        @Override
        public void invoke(@NotNull final Project project, final Editor editor, final PsiFile file) throws IncorrectOperationException {
          evalAnchor(editor, file);

          for (JSVariable e : getElementsToProcess()) {
            generateGetterFix.fixName(e);
            anchor = doAddOneMethod(project, generateGetterFix.buildFunctionText(e, null), anchor);
            anchor = doAddOneMethod(project, generateSetterFix.buildFunctionText(e, null), anchor);
          }
          generateSetterFix.createEventConstantAndImportEventIfNeeded(project, editor, anchor, getElementsToProcess());
        }
      };
    }

    return new MyBaseCreateMethodsFix(myMode, jsClass, eventBinder, makePrivate, null);
  }

  protected void collectCandidates(final JSClass clazz, final Collection<JSNamedElementNode> candidates) {
    final boolean skipVarsThatHaveGetters = (myMode == GenerationMode.Getter || myMode == GenerationMode.GetterAndSetter);
    final boolean skipVarsThatHaveSetters = (myMode == GenerationMode.Setter || myMode == GenerationMode.GetterAndSetter);
    collectJSVariables(clazz, candidates, skipVarsThatHaveGetters, skipVarsThatHaveSetters, false);
  }

  public static class MyBaseCreateMethodsFix extends BaseCreateMethodsFix<JSVariable> {
    private final GenerationMode myMode;
    @Nullable private final EventBinder myEventBinder;
    private final boolean myMakePrivate;
    private final JSCodeStyleSettings codeStyleSettings;
    private static final String PARAMETER_NAME = "value";
    @Nullable private final String myQualifier;

    public MyBaseCreateMethodsFix(final GenerationMode mode, JSClass jsClass, @Nullable EventBinder eventBinder, boolean makePrivate, @Nullable String qualifier) {
      super(jsClass);
      this.myMode = mode;
      myEventBinder = eventBinder;
      myMakePrivate = makePrivate;
      myQualifier = qualifier;
      codeStyleSettings = CodeStyleSettingsManager.getSettings(jsClass.getProject()).getCustomSettings(JSCodeStyleSettings.class);
    }

    public void invoke(@NotNull final Project project, final Editor editor, final PsiFile file) throws IncorrectOperationException {
      super.invoke(project, editor, file);
      createEventConstantAndImportEventIfNeeded(project, editor, anchor, getElementsToProcess());
    }

    private void createEventConstantAndImportEventIfNeeded(final Project project,
                                                           final Editor editor,
                                                           final PsiElement importContext,
                                                           final Collection<JSVariable> variables) {
      if ((myMode == GenerationMode.Setter || myMode == GenerationMode.GetterAndSetter) &&
          myEventBinder != null &&
          myEventBinder.isBindEvent()) {
        ImportUtils.importAndShortenReference("flash.events.Event", importContext, true, false);
        if (myEventBinder.isCreateEventConstant()) {
          createEventConstant(project, editor, variables);
        }
      }
    }

    private void createEventConstant(final Project project, final Editor editor, final Collection<JSVariable> variables) {
      assert myEventBinder != null;

      final Collection<Pair<String,String>> varNameAndAccessorNameList = new ArrayList<Pair<String, String>>(variables.size());
      for (final JSVariable variable : variables) {
        varNameAndAccessorNameList.add(Pair.create(variable.getName(), buildName(variable)));
      }

      PsiDocumentManager.getInstance(project).doPostponedOperationsAndUnblockDocument(editor.getDocument());

      for (final Pair<String, String> varNameAndAccessorName : varNameAndAccessorNameList) {
        final String eventName = myEventBinder.getEventName(varNameAndAccessorName.first);
        final String eventConstantName = myEventBinder.getEventConstantName(varNameAndAccessorName.first);
        if (myJsClass instanceof XmlBackedJSClassImpl) {
          ((XmlBackedJSClassImpl)myJsClass).clearCaches(); // otherwise findFunctionByNameAndKind(..) returns null, but getFunctions() contains requested function
        }
        final JSFunction jsFunction = myJsClass.findFunctionByNameAndKind(varNameAndAccessorName.second, JSFunction.FunctionKind.SETTER);
        
        final PsiElement eventConstantIdentifier = findEventConstantIdentifier(jsFunction, eventConstantName);
        if (eventConstantIdentifier != null && eventConstantIdentifier.isValid()) {
          final String constantDeclaration =
            MessageFormat.format("public static const {0}:String = \"{1}\";", eventConstantName, eventName);

          new CreateJSVariableIntentionAction(eventConstantName, true, true) {
            protected void buildTemplate(final Template template,
                                         final JSReferenceExpression referenceExpression,
                                         final boolean ecma,
                                         final boolean staticContext,
                                         final PsiFile file,
                                         final PsiElement anchorParent) {
              template.addTextSegment(constantDeclaration);
            }

            public void apply(final PsiElement psiElement) {
              applyFix(project, psiElement, psiElement.getContainingFile(), editor);
            }
          }.apply(eventConstantIdentifier);
        }
      }
    }

    @Override
    protected boolean shouldHandleNoTypeAsAnyType() {
      return true;
    }

    private static PsiElement findEventConstantIdentifier(final PsiElement psiElement, final String eventConstantName) {
      final Ref<PsiElement> elementRef = new Ref<PsiElement>();
      PsiTreeUtil.processElements(psiElement, new PsiElementProcessor() {
        public boolean execute(final PsiElement element) {
          if (element instanceof JSReferenceExpression && element.getText().equals(eventConstantName)) {
            elementRef.set(element);
            return false;
          }
          return true;
        }
      });

      return elementRef.get();
    }

    protected String buildFunctionBodyText(final String retType, final JSParameterList parameterList, final JSVariable func) {
      String qualifier = myQualifier != null ? myQualifier + "." : "";
      final String semicolon = codeStyleSettings.USE_SEMICOLON_AFTER_STATEMENT ? ";":"";
      String varName = func.getName();
      if (myMode == GenerationMode.Setter) {
        String checkNeedEvent = "";
        String dispatchEvent = "";
        if(myEventBinder != null && myEventBinder.isBindEvent()) {
          final String quotedEventNameOrConstant = myEventBinder.isCreateEventConstant()
                                                   ? myEventBinder.getEventConstantName(varName)
                                                   : "\"" + myEventBinder.getEventName(varName) + "\"";
          dispatchEvent = "\ndispatchEvent(new Event(" + quotedEventNameOrConstant + "))" + semicolon;
          checkNeedEvent = "if(" + varName + "==" + PARAMETER_NAME + ") return"+semicolon +"\n";
        }
        return "{\n" + checkNeedEvent + qualifier + varName + "=" + PARAMETER_NAME + semicolon + dispatchEvent + "\n}";
      } else if (myMode == GenerationMode.Getter) {
        return "{\nreturn " + qualifier + varName + semicolon + "\n}";
      }
      return " {}";
    }

    protected String buildFunctionAttrText(final String attrText, final JSAttributeList attributeList, final JSVariable function) {
      String baseText =
        "public" + (attributeList != null && attributeList.hasModifier(JSAttributeList.ModifierType.STATIC) ? " static" : "");

      if (myMode == GenerationMode.Getter && myEventBinder != null && myEventBinder.isBindEvent()) {
        baseText = "[Bindable(event=\"" + myEventBinder.getEventName(function.getName()) + "\")]\n" + baseText;
      }

      return baseText;
    }

    @Override
      protected String buildFunctionKind(final JSVariable fun) {
      if(myMode == GenerationMode.Getter) return "get ";
      if(myMode == GenerationMode.Setter) return "set ";
      return super.buildFunctionKind(fun);
    }

    protected String buildReturnType(final String typeString) {
      if(myMode == GenerationMode.Setter) return "void";
      return super.buildReturnType(typeString);
    }

    @Override
    protected void fixName(JSVariable jsVariable) {
      String newName = JSResolveUtil.transformVarNameToAccessorName(super.buildName(jsVariable), codeStyleSettings);
      String varName = jsVariable.getName();
      
      if (newName.equals(varName) && 
          codeStyleSettings.FIELD_PREFIX.length() > 0 &&
          varName != null &&
          !varName.startsWith(codeStyleSettings.FIELD_PREFIX)) {
        //RefactoringFactory.getInstance(jsVariable.getProject())
        //  .createRename(jsVariable, codeStyleSettings.FIELD_PREFIX + varName).run();
        JSVariable copy = (JSVariable)jsVariable.copy();
        String newVarName = codeStyleSettings.FIELD_PREFIX + varName;
        copy.setName(newVarName);
        for(PsiReference ref:ReferencesSearch.search(jsVariable, GlobalSearchScope.fileScope(jsVariable.getContainingFile())).findAll()) {
          if (JSResolveUtil.getClassOfContext(ref.getElement()) != myJsClass) continue;
          if (ref.getElement().getParent() != jsVariable) ref.bindToElement(copy);
        }
        jsVariable.setName(newVarName);
      }

      if (myMakePrivate) {
        JSVisibilityUtil.setVisibility(jsVariable.getAttributeList(), JSAttributeList.AccessType.PRIVATE);
      }
    }

    @Override
    protected String buildName(final JSVariable fun) {
      return JSResolveUtil.transformVarNameToAccessorName(super.buildName(fun), codeStyleSettings);
    }

    protected String buildParameterList(final JSParameterList parameterList, final JSVariable fun, MultiMap<String, String> types) {
      if (myMode == GenerationMode.Setter) {
        final String s = JSTypeEvaluateManager.getBaseArrayType(fun.getTypeString());
        return "(" + PARAMETER_NAME + (s != null ?":" + s:"")  + ")";
      }
      return (parameterList != null ? parameterList.getText():"()");
    }
  }

}

