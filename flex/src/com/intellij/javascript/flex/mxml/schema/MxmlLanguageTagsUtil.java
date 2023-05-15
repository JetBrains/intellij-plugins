// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.flex.mxml.schema;

import com.intellij.codeInsight.daemon.IdeValidationHost;
import com.intellij.codeInsight.daemon.Validator;
import com.intellij.codeInsight.intention.IntentionAction;
import com.intellij.javascript.flex.FlexPredefinedTagNames;
import com.intellij.javascript.flex.FlexStateElementNames;
import com.intellij.javascript.flex.mxml.MxmlJSClass;
import com.intellij.javascript.flex.mxml.MxmlLanguageInjector;
import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.flex.FlexBundle;
import com.intellij.lang.javascript.flex.XmlBackedJSClassImpl;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.psi.xml.*;
import com.intellij.util.ArrayUtil;
import com.intellij.util.IncorrectOperationException;
import com.intellij.xml.XmlNamespaceHelper;
import com.intellij.xml.analysis.XmlAnalysisBundle;
import com.intellij.xml.util.XmlTagUtil;
import com.intellij.xml.util.XmlUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Collections;
import java.util.Set;

import static com.intellij.lang.javascript.JavaScriptSupportLoader.*;

public final class MxmlLanguageTagsUtil {

  static final String NAME_ATTRIBUTE = "name";

  private static final String[] LANGUAGE_TAGS_ALLOWED_UNDER_ROOT_TAG = {
    FlexPredefinedTagNames.BINDING,
    FlexPredefinedTagNames.DECLARATIONS,
    FlexPredefinedTagNames.LIBRARY,
    FlexPredefinedTagNames.METADATA,
    MxmlLanguageInjector.PRIVATE_TAG_NAME,
    FlexPredefinedTagNames.SCRIPT,
    FlexPredefinedTagNames.STYLE
  };

  private static final String[] LANGUAGE_TAGS_ALLOWED_UNDER_INLINE_COMPONENT_ROOT_TAG = {
    FlexPredefinedTagNames.BINDING,
    FlexPredefinedTagNames.DECLARATIONS,
    FlexPredefinedTagNames.METADATA,
    FlexPredefinedTagNames.SCRIPT,
    FlexPredefinedTagNames.STYLE
  };

  private MxmlLanguageTagsUtil() {
  }

  static boolean isComponentTag(final XmlTag tag) {
    return tag != null && XmlBackedJSClassImpl.isComponentTag(tag);
  }

  static boolean isFxPrivateTag(final XmlTag tag) {
    return MxmlLanguageInjector.isFxPrivateTag(tag);
  }

  static boolean isXmlOrXmlListTag(final XmlTag tag) {
    return tag != null &&
           (MxmlJSClass.XML_TAG_NAME.equals(tag.getLocalName()) ||
            MxmlJSClass.XMLLIST_TAG_NAME.equals(tag.getLocalName())) &&
           (isLanguageNamespace(tag.getNamespace()));
  }

  static boolean isFxLibraryTag(final XmlTag tag) {
    return MxmlJSClass.isFxLibraryTag(tag);
  }

  static boolean isFxDefinitionTag(final XmlTag tag) {
    return tag != null && CodeContext.DEFINITION_TAG_NAME.equals(tag.getLocalName()) && MXML_URI3.equals(tag.getNamespace());
  }

  static boolean isFxDeclarationsTag(final XmlTag tag) {
    return tag != null && FlexPredefinedTagNames.DECLARATIONS.equals(tag.getLocalName()) && MXML_URI3.equals(tag.getNamespace());
  }

  public static boolean isFxReparentTag(final XmlTag tag) {
    return tag != null && CodeContext.REPARENT_TAG_NAME.equals(tag.getLocalName()) && MXML_URI3.equals(tag.getNamespace());
  }

  public static boolean isScriptTag(final XmlTag tag) {
    return tag != null && FlexPredefinedTagNames.SCRIPT.equals(tag.getLocalName()) && isLanguageNamespace(tag.getNamespace());
  }

  public static boolean isDesignLayerTag(final XmlTag tag) {
    return tag != null && FlexPredefinedTagNames.DESIGN_LAYER.equals(tag.getLocalName()) && MXML_URI3.equals(tag.getNamespace());
  }

  public static boolean isLanguageTagAllowedUnderRootTag(final XmlTag tag) {
    return tag != null &&
           (MXML_URI3.equals(tag.getNamespace()) || MXML_URI.equals(tag.getNamespace())) &&
           ArrayUtil.contains(tag.getLocalName(), LANGUAGE_TAGS_ALLOWED_UNDER_ROOT_TAG);
  }

  public static boolean isLanguageTagAllowedUnderInlineComponentRootTag(final XmlTag tag) {
    return tag != null &&
           (MXML_URI3.equals(tag.getNamespace()) || MXML_URI.equals(tag.getNamespace())) &&
           ArrayUtil.contains(tag.getLocalName(), LANGUAGE_TAGS_ALLOWED_UNDER_INLINE_COMPONENT_ROOT_TAG);
  }

  static void validateFxPrivateTag(final XmlTag tag, final Validator.ValidationHost host) {
    final XmlTag parentTag = tag.getParentTag();
    if (parentTag == null ||
        !(parentTag.getParent() instanceof XmlDocument) ||
        tag != parentTag.getSubTags()[parentTag.getSubTags().length - 1]) {
      addErrorMessage(tag, FlexBundle.message("javascript.validation.tag.must.be.last.child.of.root.tag", tag.getName()), host);
      //return;
    }
  }

  static void validateFxLibraryTag(final XmlTag tag, final Validator.ValidationHost host) {
    final XmlTag parentTag = tag.getParentTag();
    if (parentTag == null || !(parentTag.getParent() instanceof XmlDocument) || tag != parentTag.getSubTags()[0]) {
      addErrorMessage(tag, FlexBundle.message("javascript.validation.tag.must.be.first.child.of.root.tag", tag.getName()), host);
      return;
    }

    for (XmlTag subTag : tag.getSubTags()) {
      if (!isFxDefinitionTag(subTag)) {
        final String prefix = tag.getNamespacePrefix();
        final String fxDefinitionTag =
          StringUtil.isEmpty(prefix) ? CodeContext.DEFINITION_TAG_NAME : (prefix + ":" + CodeContext.DEFINITION_TAG_NAME);
        addErrorMessage(subTag, FlexBundle.message("javascript.validation.only.this.tag.is.allowed.here", fxDefinitionTag), host);
      }
    }
  }

  static void validateFxDefinitionTag(final XmlTag tag, final Validator.ValidationHost host) {
    final XmlTag parentTag = tag.getParentTag();
    if (!isFxLibraryTag(parentTag)) {
      final String prefix = tag.getNamespacePrefix();
      final String fxLibraryTag = StringUtil.isEmpty(prefix) ? FlexPredefinedTagNames.LIBRARY
                                                             : (prefix + ":" + FlexPredefinedTagNames.LIBRARY);
      addErrorMessage(tag,
                      FlexBundle
                        .message("javascript.validation.tag.must.be.direct.child.of.fx.library.tag", tag.getName(), fxLibraryTag),
                      host);
      return;
    }

    if (tag.getAttribute(NAME_ATTRIBUTE) == null) {
      addErrorMessage(tag, XmlAnalysisBundle.message("xml.inspections.element.doesnt.have.required.attribute", tag.getName(), NAME_ATTRIBUTE), host);
      return;
    }

    if (tag.getSubTags().length != 1) {
      addErrorMessage(tag, FlexBundle.message("javascript.validation.tag.must.have.exactly.one.child.tag", tag.getName()), host);
      //return;
    }
  }

  static void validateFxReparentTag(final XmlTag tag, final Validator.ValidationHost host) {
    if (tag.getAttribute(CodeContext.TARGET_ATTR_NAME) == null) {
      addErrorMessage(tag, XmlAnalysisBundle.message("xml.inspections.element.doesnt.have.required.attribute", tag.getName(), CodeContext.TARGET_ATTR_NAME),
                      host);
      return;
    }

    if (tag.getAttribute(FlexStateElementNames.INCLUDE_IN) == null &&
        tag.getAttribute(FlexStateElementNames.EXCLUDE_FROM) == null) {
      addErrorMessage(tag, FlexBundle.message("javascript.validation.tag.must.have.attribute.includein.or.excludefrom", tag.getName()), host);
      //return;
    }
  }

  public static void checkFlex4Attributes(@NotNull final XmlTag tag,
                                          @NotNull final Validator.ValidationHost host,
                                          final boolean checkStateSpecificAttrs) {
    XmlAttribute flex3NamespaceDeclaration = null;
    XmlAttribute flex4NamespaceDeclaration = null;
    XmlAttribute itemCreationPolicyAttr = null;
    XmlAttribute itemDestructionPolicyAttr = null;
    boolean includeInOrExcludeFromAttrPresent = false;

    for (final XmlAttribute attribute : tag.getAttributes()) {
      final String name = attribute.getName();

      if (attribute.isNamespaceDeclaration()) {
        final String namespace = attribute.getValue();
        switch (namespace) {
          case MXML_URI -> flex3NamespaceDeclaration = attribute;
          case MXML_URI3 -> flex4NamespaceDeclaration = attribute;
        }
      }
      else if (checkStateSpecificAttrs) {
        switch (name) {
          case FlexStateElementNames.INCLUDE_IN, FlexStateElementNames.EXCLUDE_FROM -> includeInOrExcludeFromAttrPresent = true;
          case FlexStateElementNames.ITEM_CREATION_POLICY -> itemCreationPolicyAttr = attribute;
          case FlexStateElementNames.ITEM_DESTRUCTION_POLICY -> itemDestructionPolicyAttr = attribute;
        }
      }
    }

    if (tag.getParent() instanceof XmlDocument) {
      if (flex3NamespaceDeclaration == null && flex4NamespaceDeclaration == null) {
        final String[] knownNamespaces = tag.knownNamespaces();

        boolean suggestFlex3Namespace = true;
        for (final String flex4Namespace : MxmlJSClass.FLEX_4_NAMESPACES) {
          if (ArrayUtil.contains(flex4Namespace, knownNamespaces)) {
            suggestFlex3Namespace = false;
            break;
          }
        }

        final DeclareNamespaceIntention flex4Intention = new DeclareNamespaceIntention(tag, "fx", MXML_URI3);

        final IntentionAction[] intentions = suggestFlex3Namespace ? new IntentionAction[]{flex4Intention,
          new DeclareNamespaceIntention(tag, "mx", MXML_URI)} : new IntentionAction[]{flex4Intention};

        addErrorMessage(tag, FlexBundle.message("root.tag.must.contain.language.namespace"), host, intentions);
      }
      else if (flex3NamespaceDeclaration != null && flex4NamespaceDeclaration != null) {
        addErrorMessage(flex3NamespaceDeclaration.getValueElement(), FlexBundle.message("different.language.namespaces"), host,
                        new RemoveNamespaceDeclarationIntention(flex3NamespaceDeclaration));
        addErrorMessage(flex4NamespaceDeclaration.getValueElement(), FlexBundle.message("different.language.namespaces"), host,
                        new RemoveNamespaceDeclarationIntention(flex4NamespaceDeclaration));
      }
    }
    else {
      final String[] knownNamespaces = tag.knownNamespaces();
      if (flex3NamespaceDeclaration != null && ArrayUtil.contains(MXML_URI3, knownNamespaces)) {
        addErrorMessage(flex3NamespaceDeclaration.getValueElement(), FlexBundle.message("different.language.namespaces"), host,
                        new RemoveNamespaceDeclarationIntention(flex3NamespaceDeclaration));
      }
      if (flex4NamespaceDeclaration != null && ArrayUtil.contains(MXML_URI, knownNamespaces)) {
        addErrorMessage(flex4NamespaceDeclaration.getValueElement(), FlexBundle.message("different.language.namespaces"), host,
                        new RemoveNamespaceDeclarationIntention(flex4NamespaceDeclaration));
      }
    }

    if (checkStateSpecificAttrs && !includeInOrExcludeFromAttrPresent) {
      if (itemCreationPolicyAttr != null) {
        addErrorMessage(itemCreationPolicyAttr,
                        FlexBundle.message("must.accompany.includein.or.excludefrom.attribute", itemCreationPolicyAttr.getName()), host);
      }
      if (itemDestructionPolicyAttr != null) {
        addErrorMessage(itemDestructionPolicyAttr,
                        FlexBundle.message("must.accompany.includein.or.excludefrom.attribute", itemDestructionPolicyAttr.getName()), host);
      }
    }
  }

  private static void addErrorMessage(final XmlElement element,
                                      final String message,
                                      final Validator.ValidationHost host,
                                      IntentionAction @NotNull ... intentionActions) {

    PsiElement target = element;
    PsiElement secondaryTarget = null;

    if (element instanceof XmlAttributeValue) {
      final ASTNode node = element.getNode();
      final ASTNode value = node == null ? null : XmlChildRole.ATTRIBUTE_VALUE_VALUE_FINDER.findChild(node);
      if (value instanceof PsiElement) {
        target = (PsiElement) value;
      }
    }
    else if (element instanceof XmlAttribute) {
      target = ((XmlAttribute)element).getNameElement();
    }
    else if (element instanceof XmlTag) {
      target = XmlTagUtil.getStartTagNameElement((XmlTag)element);
      secondaryTarget = XmlTagUtil.getEndTagNameElement((XmlTag)element);
    }

    if (host instanceof IdeValidationHost) {
      if (target != null) {
        ((IdeValidationHost)host).addMessageWithFixes(target, message, Validator.ValidationHost.ErrorType.ERROR, intentionActions);
      }
      if (secondaryTarget != null) {
        ((IdeValidationHost)host).addMessageWithFixes(secondaryTarget, message, Validator.ValidationHost.ErrorType.ERROR, intentionActions);
      }
    }
    else {
      if (target != null) {
        host.addMessage(target, message, Validator.ValidationHost.ErrorType.ERROR);
      }
      if (secondaryTarget != null) {
        host.addMessage(secondaryTarget, message, Validator.ValidationHost.ErrorType.ERROR);
      }
    }
  }

  public static class RemoveNamespaceDeclarationIntention implements IntentionAction {
    private final XmlAttribute myAttribute;

    public RemoveNamespaceDeclarationIntention(final @NotNull XmlAttribute attribute) {
      myAttribute = attribute;
    }

    @Override
    @NotNull
    public String getText() {
      return FlexBundle.message("remove.namespace.declaration");
    }

    @Override
    @NotNull
    public String getFamilyName() {
      return XmlAnalysisBundle.message("xml.quickfix.remove.attribute.family");
    }

    @Override
    public boolean isAvailable(final @NotNull Project project, final Editor editor, final PsiFile file) {
      return myAttribute.isValid();
    }

    @Override
    public void invoke(final @NotNull Project project, final Editor editor, final PsiFile file) throws IncorrectOperationException {
      final int offset = removeXmlAttribute(myAttribute);
      if (offset != -1) {
        editor.getCaretModel().moveToOffset(offset);
      }
    }

    /**
     * @return offset to move caret to; can be -1
     */
    public static int removeXmlAttribute(final XmlAttribute attribute) {
      final XmlTag tag = attribute.getParent();

      final XmlAttribute nextAttribute = deleteWhiteSpaceTillNextAttribute(attribute);
      if (nextAttribute == null) {
        deletePreviousWhiteSpaces(attribute);
      }

      attribute.delete();

      XmlUtil.reformatTagStart(tag);
      if (nextAttribute != null) {
        return nextAttribute.getTextRange().getStartOffset();
      }

      return -1;
    }

    @Nullable
    private static XmlAttribute deleteWhiteSpaceTillNextAttribute(final XmlAttribute attribute) {
      PsiElement nextSibling = attribute.getNextSibling();
      while (nextSibling instanceof PsiWhiteSpace) {
        final PsiElement whiteSpace = nextSibling;
        nextSibling = nextSibling.getNextSibling();
        whiteSpace.delete();
      }
      return nextSibling instanceof XmlAttribute ? (XmlAttribute)nextSibling : null;
    }

    @Nullable
    private static PsiElement deletePreviousWhiteSpaces(final XmlAttribute attribute) {
      PsiElement prevSibling = attribute.getPrevSibling();
      while (prevSibling instanceof PsiWhiteSpace) {
        final PsiElement whiteSpace = prevSibling;
        prevSibling = prevSibling.getPrevSibling();
        whiteSpace.delete();
      }

      return prevSibling;
    }

    @Override
    public boolean startInWriteAction() {
      return true;
    }
  }

  private static final class DeclareNamespaceIntention implements IntentionAction {
    private final XmlTag myRootTag;
    private final String myDefaultPrefix;
    private final String myNamespace;

    private DeclareNamespaceIntention(final XmlTag rootTag, final String defaultPrefix, final String namespace) {
      myRootTag = rootTag;
      myDefaultPrefix = defaultPrefix;
      myNamespace = namespace;
    }

    @Override
    @NotNull
    public String getText() {
      return FlexBundle.message("declare.namespace", myNamespace);
    }

    @Override
    @NotNull
    public String getFamilyName() {
      return "Declare namespace";
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
      return myRootTag.isValid();
    }

    @Override
    public boolean startInWriteAction() {
      return true;
    }

    @Override
    public void invoke(final @NotNull Project project, final Editor editor, final PsiFile file) throws IncorrectOperationException {
      if (!myRootTag.isValid()) return;

      final Set<String> usedPrefixes = myRootTag.getLocalNamespaceDeclarations().keySet();
      int postfix = 1;
      String nsPrefix = myDefaultPrefix;

      while (usedPrefixes.contains(nsPrefix)) {
        nsPrefix = myDefaultPrefix + postfix++;
      }

      XmlNamespaceHelper.getHelper(file).insertNamespaceDeclaration((XmlFile)file, editor, Collections.singleton(myNamespace), nsPrefix, null);
    }
  }
}
