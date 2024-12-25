package com.jetbrains.plugins.meteor.spacebars.templates;

import com.dmarcotte.handlebars.psi.HbHash;
import com.intellij.lang.javascript.index.FrameworkIndexingHandler;
import com.intellij.lang.javascript.index.JSCustomIndexer;
import com.intellij.lang.javascript.index.JSImplicitElementsIndex;
import com.intellij.lang.javascript.index.JSIndexContentBuilder;
import com.intellij.lang.javascript.psi.*;
import com.intellij.lang.javascript.psi.ecmal4.JSAttributeList;
import com.intellij.lang.javascript.psi.literal.JSLiteralImplicitElementProvider;
import com.intellij.lang.javascript.psi.stubs.JSElementIndexingData;
import com.intellij.lang.javascript.psi.stubs.JSImplicitElement;
import com.intellij.lang.javascript.psi.stubs.impl.JSImplicitElementImpl;
import com.intellij.lang.javascript.psi.types.JSContext;
import com.intellij.lang.javascript.psi.types.JSNamedTypeFactory;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.ArrayUtil;
import com.intellij.util.containers.ContainerUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Set;

public final class MeteorHelpersFrameworkIndexingHandler extends FrameworkIndexingHandler {
  private static final String METHOD_REGISTER_TEMPLATE_HELPERS = "helpers";
  private static final String METHOD_REGISTER_GLOBAL_HELPER = "registerHelper";
  private static final String[] INTERESTED_METHOD_NAMES = new String[]{METHOD_REGISTER_GLOBAL_HELPER};

  private static final Set<String> POSSIBLE_NAMESPACES_FOR_GLOBAL_HELPER_METHOD =
    ContainerUtil.newHashSet("Template", "Handlebars", "UI", "Blaze");
  public static final String NAMESPACE_GLOBAL_HELPERS = "MeteorGlobalHelpersNamespace";
  public static final String NAMESPACE_TEMPLATE_HELPERS = "MeteorTemplateHelpersNamespace";

  public static final String METEOR_TEMPLATE_JS_TYPE = "Template";
  public static final String METEOR_DEPRECATED_NAMESPACE_UI = "UI";

  public static final String METEOR_TEMPLATE_RESOLVED_TYPE = "I" + METEOR_TEMPLATE_JS_TYPE;
  public static final String METEOR_TEMPLATE_TAG = "template";
  public static final String HASH_NAMESPACE = "hash";

  /**
   * for Template.templateName.helpers({}) call
   */
  @Override
  public String resolveContextFromProperty(JSObjectLiteralExpression objectLiteralExpression, boolean returnPropertiesNamespace) {
    final PsiElement argumentList = objectLiteralExpression.getParent();
    if (!(argumentList instanceof JSArgumentList)) return null;

    final PsiElement callExpression = argumentList.getParent();
    if (!(callExpression instanceof JSCallExpression)) return null;

    JSExpression methodExpression = ((JSCallExpression)callExpression).getMethodExpression();
    if (!(methodExpression instanceof JSReferenceExpression)) return null;

    if (!METHOD_REGISTER_TEMPLATE_HELPERS.equals(((JSReferenceExpression)methodExpression).getReferencedName())) return null;

    JSReferenceExpression withTemplateNameExpression = getReferenceExpressionQualifier(methodExpression);
    if (withTemplateNameExpression == null) return null;

    JSReferenceExpression templateCallExpression = getReferenceExpressionQualifier(withTemplateNameExpression);
    if (templateCallExpression == null) return null;

    if (!checkTemplateReferenceExpression(templateCallExpression)) return null;

    return getTemplateNamespace(withTemplateNameExpression.getReferencedName());
  }

  public static String getTemplateNamespace(String templateName) {
    return NAMESPACE_TEMPLATE_HELPERS + "." + templateName;
  }

  @Override
  public String @NotNull [] implicitProviderMethodNames() {
    return INTERESTED_METHOD_NAMES;
  }

  @Override
  public JSLiteralImplicitElementProvider createLiteralImplicitElementProvider(@NotNull String calledMethodName) {
    assert METHOD_REGISTER_GLOBAL_HELPER.equals(calledMethodName);

    return new JSLiteralImplicitElementProvider() {
      @Override
      public void fillIndexingData(@NotNull JSLiteralExpression argument,
                                   @NotNull JSCallExpression callExpression,
                                   @NotNull JSElementIndexingData outIndexingData) {
        if (hasGlobalHelpers(callExpression)) {
          JSExpression[] arguments = callExpression.getArguments();
          if (arguments.length != 2) return;

          JSExpression nameLiteral = ArrayUtil.getFirstElement(arguments);
          if (nameLiteral != argument) return;

          if (argument.isQuotedLiteral()) {
            String text = nameLiteral.getText();
            if (StringUtil.isEmpty(text)) return;
            String unquote = StringUtil.unquoteString(text);
            if (StringUtil.isEmpty(unquote)) return;

            final JSImplicitElementImpl.Builder
              builder = new JSImplicitElementImpl.Builder(unquote, argument)
              .setNamespace(JSQualifiedNameImpl.create(NAMESPACE_GLOBAL_HELPERS, null));
            outIndexingData.addImplicitElement(builder.toImplicitElement());
          }
        }
      }
    };
  }


  @Override
  public boolean processCustomElement(@NotNull PsiElement element, @NotNull JSIndexContentBuilder builder) {
    if (element instanceof XmlTag) {
      return processTag((XmlTag)element, builder);
    }
    else if (element instanceof HbHash) {
      return processSpacebarsHash((HbHash)element, builder);
    }

    return true;
  }

  private boolean processSpacebarsHash(@NotNull HbHash element, JSIndexContentBuilder builder) {
    String name = element.getHashName();
    if (StringUtil.isEmpty(name)) return true;

    final PsiElement nameElement = element.getHashNameElement();
    assert nameElement != null;

    final JSImplicitElementImpl.Builder elementBuilder = new JSImplicitElementImpl.Builder(name, null);

    elementBuilder
      .setNamespace(JSNamedTypeFactory.createNamespace(JSQualifiedNameImpl.create(HASH_NAMESPACE, null), JSContext.INSTANCE, null, false))
      .setAccessType(JSAttributeList.AccessType.PUBLIC)
      .setType(JSImplicitElement.Type.Property);

    final JSImplicitElementsIndex.JSElementProxy proxy =
      new JSImplicitElementsIndex.JSElementProxy(elementBuilder, nameElement.getTextRange().getStartOffset());
    builder.addImplicitElement(name, proxy);

    return false;
  }

  private static boolean processTag(@NotNull XmlTag tag, @NotNull JSIndexContentBuilder builder) {
    if (!METEOR_TEMPLATE_TAG.equals(tag.getName())) return true;

    final XmlAttribute nameAttribute = tag.getAttribute("name");
    if (nameAttribute == null) return true;

    final String value = nameAttribute.getValue();
    if (value == null) return true;

    final JSImplicitElementImpl.Builder elementBuilder = new JSImplicitElementImpl.Builder(value, null)
      .setNamespace(JSNamedTypeFactory.createNamespace(JSQualifiedNameImpl.create(METEOR_TEMPLATE_RESOLVED_TYPE, null),
                                                       JSContext.INSTANCE, null, false))
      .setAccessType(JSAttributeList.AccessType.PUBLIC)
      .setType(JSImplicitElement.Type.Tag);


    JSCustomIndexer.addImplicitElement(nameAttribute, elementBuilder, builder);

    return false;
  }

  /**
   * call expression is 'Template.registerHelper(...)'
   */
  private static boolean hasGlobalHelpers(@NotNull JSCallExpression callExpression) {
    JSExpression methodExpression = callExpression.getMethodExpression();
    if (!(methodExpression instanceof JSReferenceExpression)) return false;

    if (!METHOD_REGISTER_GLOBAL_HELPER.equals(((JSReferenceExpression)methodExpression).getReferencedName())) return false;

    JSReferenceExpression withNamespaceQualifier = getReferenceExpressionQualifier(methodExpression);

    return withNamespaceQualifier != null &&
           POSSIBLE_NAMESPACES_FOR_GLOBAL_HELPER_METHOD.contains(withNamespaceQualifier.getReferencedName());
  }

  private static @Nullable JSReferenceExpression getReferenceExpressionQualifier(@Nullable JSExpression expression) {
    if (expression instanceof JSReferenceExpression) {
      JSExpression qualifier = ((JSReferenceExpression)expression).getQualifier();
      return qualifier instanceof JSReferenceExpression ? (JSReferenceExpression)qualifier : null;
    }
    return null;
  }

  @Override
  public boolean canProcessCustomElement(@NotNull PsiElement element) {
    return element instanceof XmlTag || element instanceof HbHash;
  }

  /**
   * check 'Template' ref expression
   */
  public static boolean checkTemplateReferenceExpression(@NotNull JSReferenceExpression type) {
    String context = type.getReferencedName();
    if (context == null || type.getQualifier() != null) {
      return false;
    }

    return METEOR_TEMPLATE_JS_TYPE.equals(context) || METEOR_DEPRECATED_NAMESPACE_UI.equals(context);
  }
}
