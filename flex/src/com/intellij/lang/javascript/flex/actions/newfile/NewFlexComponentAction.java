package com.intellij.lang.javascript.flex.actions.newfile;

import com.intellij.javascript.flex.mxml.MxmlJSClass;
import com.intellij.javascript.flex.mxml.schema.CodeContext;
import com.intellij.javascript.flex.mxml.schema.CodeContextHolder;
import com.intellij.javascript.flex.mxml.schema.FlexSchemaHandler;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.projectStructure.model.FlexBuildConfigurationManager;
import com.intellij.lang.javascript.validation.fixes.ActionScriptCreateClassOrInterfaceFix;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtilCore;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ArrayUtil;
import org.jetbrains.annotations.NonNls;

import java.util.Collection;

public class NewFlexComponentAction extends NewActionScriptClassAction {

  @NonNls private static final String FLEX3_COMPONENT_TEMPLATE_NAME = "Flex 3 Component";
  @NonNls private static final String FLEX4_COMPONENT_TEMPLATE_NAME = "Flex 4 Component";

  private static final String[] FLEX_CLASSIFIER_TEMPLATES =
    new String[]{FLEX3_COMPONENT_TEMPLATE_NAME, FLEX4_COMPONENT_TEMPLATE_NAME};

  public static boolean isClassifierTemplate(String templateName) {
    return ArrayUtil.contains(templateName, FLEX_CLASSIFIER_TEMPLATES);
  }

  @Override
  protected boolean isAvailableIn(final Module module) {
    return super.isAvailableIn(module) && !FlexBuildConfigurationManager.getInstance(module).getActiveConfiguration().isPureAs();
  }

  @Override
  protected ActionScriptCreateClassOrInterfaceFix createAction(final PsiDirectory dir) {
    return new CreateFlexComponentFix(dir);
  }

  @Override
  protected String getCommandName() {
    return FlexBundle.message("new.flex.component.command.name");
  }

  public static void setParentComponent(final MxmlJSClass clazz, final String newParentQname) {
    Pair<String, String> prefixAndNamespace = getPrefixAndNamespace(clazz.getParent(), newParentQname);
    clazz.setBaseComponent(newParentQname, prefixAndNamespace.first, prefixAndNamespace.second);
  }

  public static Pair<String, String> getPrefixAndNamespace(XmlTag tag, String qName) {
    Module module = ModuleUtilCore.findModuleForPsiElement(tag);
    boolean isFlex4Template = ArrayUtil.contains(JavaScriptSupportLoader.MXML_URI3, tag.knownNamespaces());

    GlobalSearchScope resolveScope = tag.getResolveScope();

    CodeContextHolder holder = CodeContextHolder.getInstance(module.getProject());
    // ensure namespace is loaded into code context (including all the namespaces from all the libraries)
    CodeContext.getContext(MxmlJSClass.MXML_URI4, module, resolveScope);
    Collection<String> namespaces = holder.getNamespaces(module, resolveScope);
    String[] illegalNamespaces =
      isFlex4Template ? new String[]{JavaScriptSupportLoader.MXML_URI} : MxmlJSClass.FLEX_4_NAMESPACES;
    for (String namespace : namespaces) {
      if (ArrayUtil.contains(namespace, illegalNamespaces) || CodeContext.isPackageBackedNamespace(namespace)) {
        continue;
      }
      CodeContext codeContext = CodeContext.isStdNamespace(namespace)
        ? holder.getStandardContext(namespace, module)
        : holder.getCodeContext(namespace, module, resolveScope);
      if (codeContext == null) {
        continue;
      }
      if (codeContext.getElementDescriptor(StringUtil.getShortName(qName), qName) != null) {
        return Pair.create(FlexSchemaHandler.getDefaultPrefix(namespace), namespace);
      }
    }

    String packageName = StringUtil.getPackageName(qName);
    String namespace = StringUtil.isEmpty(packageName) ? "*" : packageName + ".*";
    return Pair.create(FlexSchemaHandler.getDefaultPrefix(namespace), namespace);
  }
}
