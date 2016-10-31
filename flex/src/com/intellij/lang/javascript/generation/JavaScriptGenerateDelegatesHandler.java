package com.intellij.lang.javascript.generation;

import com.intellij.codeInsight.CodeInsightBundle;
import com.intellij.ide.util.MemberChooser;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.psi.impl.JSPsiImplUtils;
import com.intellij.lang.javascript.psi.resolve.JSInheritanceUtil;
import com.intellij.lang.javascript.psi.resolve.JSResolveUtil;
import com.intellij.lang.javascript.psi.resolve.ResolveProcessor;
import com.intellij.lang.javascript.psi.util.JSUtils;
import com.intellij.lang.javascript.validation.fixes.BaseCreateMethodsFix;
import com.intellij.lang.javascript.validation.fixes.JSAttributeListWrapper;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.ui.DialogWrapper;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.ResolveState;
import com.intellij.util.ArrayUtil;
import com.intellij.util.containers.MultiMap;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

public class JavaScriptGenerateDelegatesHandler extends BaseJSGenerateHandler {

  public static final String[] PRIMITIVE_TYPES = JSCommonTypeNames.ALL;

  private static final Logger LOG = Logger.getInstance("#com.intellij.lang.javascript.generation.JavaScriptGenerateDelegatesHandler");


  @Override
  public boolean isValidFor(Editor editor, PsiFile file) {
    if (!super.isValidFor(editor, file)) {
      return false;
    }
    return !findCandidateFields(findClass(file, editor)).isEmpty();
  }

  @Override
  public void invoke(@NotNull Project project, @NotNull Editor editor, @NotNull PsiFile file) {
    final JSClass jsClass = findClass(file, editor);
    if (jsClass == null) return;

    Collection<JSField> fields = findCandidateFields(jsClass);

    final JSField field;
    if (ApplicationManager.getApplication().isUnitTestMode()) {
      LOG.assertTrue(fields.size() == 1);
      field = fields.iterator().next();
    }
    else {
      final MemberChooser<JSNamedElementNode> targetChooser = createMemberChooserDialog(project, jsClass, wrap(fields), false, false,
                                                                                        CodeInsightBundle
                                                                                          .message(
                                                                                            "generate.delegate.target.chooser.title"));
      targetChooser.show();
      if (targetChooser.getExitCode() != DialogWrapper.OK_EXIT_CODE) return;
      field = (JSField)targetChooser.getSelectedElements().get(0).getPsiElement();
    }

    JSType fieldType = field.getType();
    if (fieldType == null) return;
    JSClass fieldClass = fieldType.resolveClass();
    if (fieldClass == null) return;

    final boolean allowPackageLocal = !JSPsiImplUtils.differentPackageName(StringUtil.getPackageName(fieldClass.getQualifiedName()),
                                                                           StringUtil.getPackageName(jsClass.getQualifiedName()));

    // don't add members along with their supers
    class MemberDescriptor {
      private final String name;
      @Nullable private final JSFunction.FunctionKind kind;

      public MemberDescriptor(JSFunction method) {
        name = method.getName();
        kind = method.getKind();
      }

      public MemberDescriptor(JSVariable field) {
        name = field.getName();
        kind = null;
      }

      @Override
      public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        MemberDescriptor that = (MemberDescriptor)o;

        if (kind != that.kind) return false;
        if (!name.equals(that.name)) return false;

        return true;
      }

      @Override
      public int hashCode() {
        int result = name.hashCode();
        result = 31 * result + (kind != null ? kind.hashCode() : 0);
        return result;
      }
    }

    final Map<MemberDescriptor, JSNamedElement> memberCandidates = new HashMap<>();
    ResolveProcessor p = new ResolveProcessor(null) {
      {
        setToProcessHierarchy(true);
      }

      @Override
      public boolean execute(@NotNull PsiElement element, @NotNull ResolveState state) {
        JSClass clazz = JSUtils.getMemberContainingClass(element);
        if (clazz == null || JSResolveUtil.isObjectClass(clazz) || clazz == jsClass) {
          return true;
        }

        if (element instanceof JSFunction) {
          JSFunction method = (JSFunction)element;
          if (memberCandidates.containsKey(method.getName())) {
            return true;
          }

          JSAttributeList attributeList = method.getAttributeList();
          if (attributeList.getAccessType() == JSAttributeList.AccessType.PRIVATE ||
              attributeList.getAccessType() == JSAttributeList.AccessType.PROTECTED) {
            return true;
          }
          if (!allowPackageLocal &&
              attributeList.getNamespace() == null &&
              attributeList.getAccessType() == JSAttributeList.AccessType.PACKAGE_LOCAL) {
            return true;
          }

          if (method.getKind() == JSFunction.FunctionKind.CONSTRUCTOR) {
            return true;
          }
          if (attributeList.hasModifier(JSAttributeList.ModifierType.STATIC)) {
            return true;
          }

          if (JSInheritanceUtil.findMethodInClass(method, jsClass, true) != null) {
            return true;
          }
          memberCandidates.put(new MemberDescriptor(method), method);
        }
        else if (element instanceof JSVariable) {
          JSVariable f = (JSVariable)element;

          if (memberCandidates.containsKey(f.getName())) {
            return true;
          }

          JSAttributeList attributeList = f.getAttributeList();
          if (attributeList.getAccessType() == JSAttributeList.AccessType.PRIVATE ||
              attributeList.getAccessType() == JSAttributeList.AccessType.PROTECTED) {
            return true;
          }
          if (!allowPackageLocal && attributeList.getAccessType() == JSAttributeList.AccessType.PACKAGE_LOCAL) {
            return true;
          }

          if (jsClass.findFunctionByName(f.getName()) != null) {
            return true;
          }

          memberCandidates.put(new MemberDescriptor(f), f);
        }

        return true;
      }
    };

    fieldClass.processDeclarations(p, ResolveState.initial(), fieldClass, fieldClass);
    Collection<JSNamedElementNode> selected;
    if (ApplicationManager.getApplication().isUnitTestMode()) {
      LOG.assertTrue(!memberCandidates.isEmpty());
      selected = wrap(memberCandidates.values());
    }
    else {
      final MemberChooser<JSNamedElementNode> methodsChooser =
        createMemberChooserDialog(project, jsClass, wrap(memberCandidates.values()), false,
                                  true, CodeInsightBundle.message("generate.delegate.method.chooser.title"));
      methodsChooser.show();
      if (methodsChooser.getExitCode() != DialogWrapper.OK_EXIT_CODE) return;
      selected = methodsChooser.getSelectedElements();
    }

    BaseCreateMethodsFix fix = new BaseCreateMethodsFix<JSNamedElement>(jsClass) {

      final JavaScriptGenerateAccessorHandler.MyBaseCreateMethodsFix generateGetterFix =
        new JavaScriptGenerateAccessorHandler.MyBaseCreateMethodsFix(JavaScriptGenerateAccessorHandler.GenerationMode.Getter, jsClass,
                                                                     null, false, field.getName());
      final JavaScriptGenerateAccessorHandler.MyBaseCreateMethodsFix generateSetterFix =
        new JavaScriptGenerateAccessorHandler.MyBaseCreateMethodsFix(JavaScriptGenerateAccessorHandler.GenerationMode.Setter, jsClass,
                                                                     null, false, field.getName());

      @Override
      protected void adjustAttributeList(JSAttributeListWrapper attributeListWrapper, JSNamedElement function) {
        attributeListWrapper.overrideAccessType(JSAttributeList.AccessType.PUBLIC);
        attributeListWrapper
          .overrideModifier(JSAttributeList.ModifierType.STATIC, field.getAttributeList().hasModifier(JSAttributeList.ModifierType.STATIC));
        attributeListWrapper.overrideModifiers(false, JSAttributeList.ModifierType.NATIVE, JSAttributeList.ModifierType.DYNAMIC,
                                               JSAttributeList.ModifierType.FINAL, JSAttributeList.ModifierType.OVERRIDE,
                                               JSAttributeList.ModifierType.VIRTUAL);
      }

      @Override
      protected void processElements(Project project, MultiMap<String, String> types, Set<JSNamedElement> elementsToProcess) {
        for (JSNamedElement e : elementsToProcess) {
          if (e instanceof JSFunction) {
            anchor = doAddOneMethod(project, buildFunctionText(e, types), anchor);
          }
          else {
            anchor = doAddOneMethod(project, generateGetterFix.buildFunctionText(e, types), anchor);
            anchor = doAddOneMethod(project, generateSetterFix.buildFunctionText(e, types), anchor);
          }
        }
      }

      @Override
      protected String buildFunctionBodyText(final String retType, final JSParameterList parameterList, final JSNamedElement element) {
        return OverrideMethodsFix.buildDelegatingText(retType, parameterList, ((JSFunction)element), field.getName(),
                                                      anchor != null ? anchor : myJsClass);
      }
    };
    doInvoke(project, editor, file, selected, fix);
  }

  private static Collection<JSNamedElementNode> wrap(Collection<? extends JSNamedElement> items) {
    final List<JSNamedElementNode> targetCandidates = new ArrayList<>(items.size());
    for (JSNamedElement field : items) {
      targetCandidates.add(new JSNamedElementNode(field));
    }
    if (ApplicationManager.getApplication().isUnitTestMode()) {
      Collections.sort(targetCandidates, (o1, o2) -> o1.getText().compareTo(o2.getText()));
    }
    return targetCandidates;
  }

  @Override
  protected String getTitleKey() {
    return null;
  }

  @Override
  protected void collectCandidates(JSClass clazz, Collection<JSNamedElementNode> candidates) {
    Collection<JSField> fields = findCandidateFields(clazz);
    for (JSField field : fields) {
      candidates.add(new JSNamedElementNode(field));
    }
  }

  @Override
  protected BaseCreateMethodsFix createFix(JSClass clazz) {
    return null;
  }

  private static Collection<JSField> findCandidateFields(JSClass clazz) {
    Collection<JSField> result = new ArrayList<>();
    for (JSField field : clazz.getFields()) {
      JSType type = field.getType();
      JSClass fieldType = type != null ? type.resolveClass() : null;
      if (fieldType != null &&
          !ArrayUtil.contains(fieldType.getQualifiedName(), PRIMITIVE_TYPES) &&
          !JSInheritanceUtil.isParentClass(clazz, fieldType, false)) {
        result.add(field);
      }
    }
    return result;
  }
}
