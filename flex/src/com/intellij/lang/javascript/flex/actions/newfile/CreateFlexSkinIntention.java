package com.intellij.lang.javascript.flex.actions.newfile;

import com.intellij.javascript.flex.mxml.schema.ClassBackedElementDescriptor;
import com.intellij.javascript.flex.resolve.ActionScriptClassResolver;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.psi.ecmal4.JSAttribute;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeNameValuePair;
import com.intellij.lang.javascript.psi.ecmal4.JSClass;
import com.intellij.lang.javascript.validation.fixes.ActionScriptCreateClassOrInterfaceFix;
import com.intellij.lang.javascript.validation.fixes.CreateMxmlFileIntentionBase;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.util.Pair;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiDirectory;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlAttributeValue;
import com.intellij.psi.xml.XmlTag;
import com.intellij.xml.XmlElementDescriptor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class CreateFlexSkinIntention extends CreateMxmlFileIntentionBase {

  public CreateFlexSkinIntention(final String skinFqn, final @NotNull PsiElement element) {
    super(skinFqn, element);
  }

  @Override
  @NotNull
  public String getText() {
    return FlexBundle.message("create.skin", myClassName);
  }

  @Override
  protected Pair<String, PsiDirectory> getFileTextAndDir(final @NotNull Module module) {
    final String hostComponent;
    final PsiDirectory targetDirectory;

    final JSClass hostComponentClass = getHostComponentClass();
    final String defaultHostComponent = hostComponentClass == null ? "" : hostComponentClass.getQualifiedName();
    if (ApplicationManager.getApplication().isUnitTestMode()) {
      hostComponent = defaultHostComponent;
      targetDirectory = WriteAction
        .compute(() -> ActionScriptCreateClassOrInterfaceFix.findOrCreateDirectory(myPackageName, myElement));
    }
    else {
      final CreateFlexSkinDialog dialog = new CreateFlexSkinDialog(module, myClassName, myPackageName,
                                                                   defaultHostComponent,
                                                                   myElement.getContainingFile());
      if (!dialog.showAndGet()) {
        return Pair.empty();
      }

      hostComponent = dialog.getHostComponent();
      targetDirectory = dialog.getTargetDirectory();
    }

    return Pair.create(getSkinContent(hostComponent, myElement), targetDirectory);
  }

  private static String getSkinContent(final String hostComponent, final PsiElement context) {
    final StringBuilder builder = new StringBuilder();
    builder.append("<?xml version=\"1.0\" encoding=\"utf-8\"?>\n");
    // TODO insert MXML file header
    builder.append("<s:Skin xmlns:fx=\"http://ns.adobe.com/mxml/2009\" xmlns:s=\"library://ns.adobe.com/flex/spark\">\n");
    builder.append("\n");

    if (!StringUtil.isEmpty(hostComponent)) {
      builder.append("<fx:Metadata>\n");
      builder.append("[HostComponent(\"").append(hostComponent).append("\")]\n");
      builder.append("</fx:Metadata>\n");
      builder.append("\n");
    }

    final PsiElement element = ActionScriptClassResolver.findClassByQNameStatic(hostComponent, context);
    if (element instanceof JSClass jsClass) {
      final Collection<String> skinStates = getSkinStates(jsClass);
      if (!skinStates.isEmpty()) {
        builder.append("<s:states>\n");
        for (final String skinState : skinStates) {
          builder.append("<s:State name=\"").append(skinState).append("\"/>\n");
        }
        builder.append("</s:states>\n");
        builder.append("\n");
      }
    }

    builder.append("</s:Skin>\n");
    return builder.toString();
  }

  private static Collection<String> getSkinStates(final JSClass jsClass) {
    final Collection<String> skinStates = new ArrayList<>();
    appendSkinStates(skinStates, jsClass, new HashSet<>());
    return skinStates;
  }

  private static void appendSkinStates(final Collection<String> skinStates, final JSClass jsClass, final Set<JSClass> visited) {
    visited.add(jsClass);

    final JSAttributeList attributeList = jsClass.getAttributeList();
    if (attributeList != null) {
      final JSAttribute[] attributes = attributeList.getAttributesByName("SkinState");
      for (final JSAttribute attribute : attributes) {
        final JSAttributeNameValuePair pair = attribute.getValueByName(null);
        if (pair != null) {
          final String state = pair.getSimpleValue();
          if (!skinStates.contains(state)) {
            skinStates.add(state);
          }
        }
      }
    }
    for (final JSClass superClass : jsClass.getSuperClasses()) {
      if (!visited.contains(superClass)) {
        appendSkinStates(skinStates, superClass, visited);
      }
    }
  }

  @Nullable
  private JSClass getHostComponentClass() {
    final XmlTag tag = myElement instanceof XmlTag
                       ? ((XmlTag)myElement).getParentTag()
                       : myElement instanceof XmlAttributeValue ? (XmlTag)myElement.getParent().getParent() : null;
    final XmlElementDescriptor descriptor = tag == null ? null : tag.getDescriptor();
    if (descriptor instanceof ClassBackedElementDescriptor) {
      final PsiElement declaration = descriptor.getDeclaration();
      if (declaration instanceof JSClass) {
        return (JSClass)declaration;
      }
    }
    return null;
  }
}
