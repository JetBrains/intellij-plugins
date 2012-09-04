package com.intellij.lang.javascript.flex.actions.newfile;

import com.intellij.codeInsight.template.TemplateBuilderImpl;
import com.intellij.codeInsight.template.impl.MacroCallNode;
import com.intellij.codeInsight.template.macro.CompleteMacro;
import com.intellij.javascript.flex.mxml.schema.CodeContext;
import com.intellij.javascript.flex.mxml.schema.CodeContextHolder;
import com.intellij.javascript.flex.mxml.schema.FlexSchemaHandler;
import com.intellij.lang.javascript.JSBundle;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.lang.javascript.validation.fixes.CreateClassOrInterfaceAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.module.ModuleUtil;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiFile;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.psi.xml.XmlToken;
import com.intellij.util.ArrayUtil;
import com.intellij.xml.util.XmlTagUtil;
import icons.JavaScriptLanguageIcons;
import org.jetbrains.annotations.Nullable;

import java.util.Collection;
import java.util.Map;

public class NewFlexComponentAction extends NewJSClassActionBase {

  public NewFlexComponentAction() {
    super(JSBundle.message("new.flex.component.action.title"), JSBundle.message("new.flex.component.action.description"),
          JavaScriptLanguageIcons.Flex.XmlBackedClass, CreateClassOrInterfaceAction.FLEX_TEMPLATES_EXTENSIONS);
  }

  @Override
  protected String getActionName(PsiDirectory directory, String newName, String templateName) {
    return JSBundle.message("new.flex.component.command.name");
  }

  @Override
  protected String getDialogTitle() {
    return JSBundle.message("new.flex.component.dialog.title");
  }

  @Override
  protected void postProcess(final PsiFile file,
                             final String templateName,
                             @Nullable final Map<String, String> customProperties) {
    ApplicationManager.getApplication().runWriteAction(new Runnable() {
      @Override
      public void run() {
        fillParentComponent(file, templateName, customProperties);
      }
    });
  }

  public static void fillParentComponent(final PsiFile file, String templateName, final Map<String, String> customProperties) {
    if (!CreateClassOrInterfaceAction.FLEX_CLASSIFIER_TEMPLATES.contains(templateName)) {
      return;
    }

    if (!(file instanceof XmlFile) || ((XmlFile)file).getDocument() == null) {
      return;
    }
    String parentComponent = customProperties != null ? customProperties.get(NewFlexComponentDialog.PARENT_COMPONENT) : null;
    if (StringUtil.isNotEmpty(parentComponent)) {
      final XmlTag rootTag = ((XmlFile)file).getDocument().getRootTag();
      if (rootTag == null) {
        return;
      }

      Pair<String, String> requiredPrefixAndNamespace = getPrefixAndNamespace(rootTag, parentComponent);
      XmlBackedJSClassImpl.setBaseComponent(rootTag, parentComponent, requiredPrefixAndNamespace.first, requiredPrefixAndNamespace.second);
    }
    else {
      Document document = PsiDocumentManager.getInstance(file.getProject()).getDocument(file);
      if (document == null) {
        return;
      }

      final Runnable runnable = new Runnable() {
        public void run() {
          XmlTag rootTag = ((XmlFile)file).getDocument().getRootTag();
          if (rootTag == null) {
            return;
          }

          XmlToken openingNode = XmlTagUtil.getStartTagNameElement(rootTag);
          if (openingNode == null) {
            return;
          }
          XmlToken closingNode = XmlTagUtil.getEndTagNameElement(rootTag);
          if (closingNode == null) {
            return;
          }

          // invoke completion for parent component
          TemplateBuilderImpl builder = new TemplateBuilderImpl(rootTag);
          builder.replaceElement(openingNode, "openingNode", new MacroCallNode(new CompleteMacro()), true);

          // closing tag text should follow opening tag text
          builder.replaceElement(closingNode, "closingNode", "openingNode", false);
          builder.run();
        }
      };

      if (ModalityState.current().equals(ModalityState.NON_MODAL)) {
        runnable.run();
      }
      else {
        // otherwise code highlighting will be disabled forever (IDEA-61708)
        ApplicationManager.getApplication().invokeLater(new Runnable() {
          @Override
          public void run() {
            ApplicationManager.getApplication().runWriteAction(runnable);
          }
        }, ModalityState.NON_MODAL);
      }
    }
  }

  public static Pair<String, String> getPrefixAndNamespace(XmlTag tag, String qName) {
    Module module = ModuleUtil.findModuleForPsiElement(tag);
    boolean isFlex4Template = ArrayUtil.contains(JavaScriptSupportLoader.MXML_URI3, tag.knownNamespaces());

    CodeContextHolder holder = CodeContextHolder.getInstance(module.getProject());
    Collection<String> namespaces = holder.getNamespaces(module);
    String[] illegalNamespaces =
      isFlex4Template ? new String[]{JavaScriptSupportLoader.MXML_URI} : JavaScriptSupportLoader.FLEX_4_NAMESPACES;
    for (String namespace : namespaces) {
      if (ArrayUtil.contains(namespace, illegalNamespaces) || CodeContext.isPackageBackedNamespace(namespace)) {
        continue;
      }
      CodeContext codeContext =
        CodeContext.isStdNamespace(namespace) ? holder.getStandardContext(namespace, module) : holder.getCodeContext(namespace, module);
      if (codeContext == null) {
        continue;
      }
      if (codeContext.getElementDescriptor(StringUtil.getShortName(qName), qName) != null) {
        return Pair.create(FlexSchemaHandler.getDefaultPrefix(namespace), namespace);
      }
    }
    String packageName = StringUtil.getPackageName(qName);
    return Pair.create("local", StringUtil.isEmpty(packageName) ? "*" : packageName + ".*");
  }

}
