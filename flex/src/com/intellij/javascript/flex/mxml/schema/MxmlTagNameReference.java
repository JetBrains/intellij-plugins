// Copyright 2000-2024 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package com.intellij.javascript.flex.mxml.schema;

import com.intellij.lang.ASTNode;
import com.intellij.lang.javascript.flex.AnnotationBackedDescriptor;
import com.intellij.lang.javascript.flex.FlexSupportLoader;
import com.intellij.lang.javascript.psi.JSFile;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.openapi.util.Comparing;
import com.intellij.openapi.util.Ref;
import com.intellij.openapi.util.TextRange;
import com.intellij.openapi.util.io.FileUtil;
import com.intellij.openapi.util.io.FileUtilRt;
import com.intellij.openapi.util.text.StringUtil;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.PsiReference;
import com.intellij.psi.XmlElementFactory;
import com.intellij.psi.codeStyle.CodeStyleManager;
import com.intellij.psi.impl.source.xml.SchemaPrefix;
import com.intellij.psi.impl.source.xml.SchemaPrefixReference;
import com.intellij.psi.impl.source.xml.TagNameReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.searches.ReferencesSearch;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.IncorrectOperationException;
import com.intellij.xml.XmlAttributeDescriptor;
import com.intellij.xml.XmlElementDescriptor;
import org.jetbrains.annotations.NonNls;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MxmlTagNameReference extends TagNameReference {
  public MxmlTagNameReference(ASTNode nameElement, boolean startTagFlag) {
    super(nameElement, startTagFlag);
  }

  @Override
  public PsiElement bindToElement(final @NotNull PsiElement element) throws IncorrectOperationException {
    final String newPackage = getNewPackage(element);
    if (newPackage == null) {
      return super.bindToElement(element);
    }
    else {
      final XmlTag tag = getTagElement();
      if (tag == null) return null;

      final String newNamespace = newPackage.isEmpty() ? "*" : newPackage + ".*";
      String newPrefix = tag.getPrefixByNamespace(newNamespace);

      if (newPrefix == null) {
        final XmlFile xmlFile = (XmlFile)tag.getContainingFile();
        newPrefix = FlexSchemaHandler.getUniquePrefix(newNamespace, xmlFile);
        final XmlTag rootTag = xmlFile.getRootTag();
        assert rootTag != null;
        insertNamespaceDeclaration(rootTag, newNamespace, newPrefix);
      }

      final SchemaPrefixReference schemaPrefixReference = getSchemaPrefixReference(tag);
      final SchemaPrefix schemaPrefix = schemaPrefixReference == null ? null : schemaPrefixReference.resolve();

      final String oldPrefix = tag.getNamespacePrefix();
      final String newLocalName = FileUtilRt.getNameWithoutExtension(((PsiFile)element).getName());
      tag.setName(StringUtil.isEmpty(newPrefix) ? newLocalName : (newPrefix + ":" + newLocalName));

      fixSubTagsPrefixes(tag, oldPrefix, newPrefix);

      removeNamespaceDeclarationIfNotUsed(schemaPrefix);

      return tag;
    }
  }

  private static void fixSubTagsPrefixes(final XmlTag tag, final String oldPrefix, final String newPrefix) {
    final XmlElementDescriptor descriptor = tag.getDescriptor();
    if (!(descriptor instanceof ClassBackedElementDescriptor)) {
      return;
    }

    for (final XmlTag subTag : tag.getSubTags()) {
      if (Comparing.strEqual(subTag.getNamespacePrefix(), oldPrefix) && subTag.getDescriptor() == null) {
        final String oldSubTagName = subTag.getName();
        subTag.setName(StringUtil.isEmpty(newPrefix) ? subTag.getLocalName() : (newPrefix + ":" + subTag.getLocalName()));
        final XmlElementDescriptor subTagDescriptor = descriptor.getElementDescriptor(subTag, tag);
        if (!(subTagDescriptor instanceof AnnotationBackedDescriptor)) {
          subTag.setName(oldSubTagName);
        }
      }
    }
  }

  private static void removeNamespaceDeclarationIfNotUsed(final SchemaPrefix schemaPrefix) {
    if (schemaPrefix == null) return;

    final Ref<Boolean> hasUsagesRef = new Ref<>(false);
    ReferencesSearch.search(schemaPrefix, GlobalSearchScope.fileScope(schemaPrefix.getContainingFile()))
      .forEach(reference -> {
        final TextRange range = schemaPrefix.getTextRange();
        if (range != null
            && (reference.getElement().getTextRange().getStartOffset() + reference.getRangeInElement().getStartOffset()
                == range.getStartOffset())
            && reference.getRangeInElement().getLength() == range.getLength()) {
          // self reference
          return true;
        }

        hasUsagesRef.set(true);
        return false;
      });

    if (!hasUsagesRef.get()) {
      final XmlAttribute attribute = schemaPrefix.getDeclaration();
      MxmlLanguageTagsUtil.RemoveNamespaceDeclarationIntention.removeXmlAttribute(attribute);
    }
  }

  private static @Nullable SchemaPrefixReference getSchemaPrefixReference(final XmlTag tag) {
    for (final PsiReference reference : tag.getReferences()) {
      if (reference instanceof SchemaPrefixReference) {
        return (SchemaPrefixReference)reference;
      }
    }
    return null;
  }

  private static @Nullable String getNewPackage(final PsiElement element) {
    /*
    if (element instanceof JSFile) {
      final JSPackageStatement packageStatement = JSPsiImplUtils.findPackageStatement((JSFile)element);
      if (packageStatement != null) {
        final String qualifiedName = packageStatement.getQualifiedName();
        return StringUtil.notNullize(qualifiedName);
      }
    }
    */
    if (element instanceof JSFile || (element instanceof XmlFile && FlexSupportLoader.isMxmlOrFxgFile((XmlFile)element))) {
      final VirtualFile virtualFile = ((PsiFile)element).getVirtualFile();
      if (virtualFile != null) {
        final VirtualFile sourceRoot =
          ProjectRootManager.getInstance(element.getProject()).getFileIndex().getSourceRootForFile(virtualFile);
        if (sourceRoot != null) {
          final String relPath = FileUtil.getRelativePath(sourceRoot.getPath(), virtualFile.getPath(), '/');
          final int lastSlashIndex = relPath.lastIndexOf("/");
          return relPath.substring(0, Math.max(0, lastSlashIndex)).replace("/", ".");
        }
      }
    }

    return null;
  }

  public static void insertNamespaceDeclaration(final @NotNull XmlTag tag, final @NotNull String namespace, final @NotNull String prefix) {
    final XmlAttribute[] attributes = tag.getAttributes();
    XmlAttribute anchor = null;
    for (final XmlAttribute attribute : attributes) {
      final XmlAttributeDescriptor descriptor = attribute.getDescriptor();
      if (attribute.isNamespaceDeclaration() || (descriptor != null && descriptor.isRequired())) {
        anchor = attribute;
      }
      else {
        break;
      }
    }

    final @NonNls String qname = "xmlns" + (!prefix.isEmpty() ? ":" + prefix : "");
    final XmlAttribute attribute = XmlElementFactory.getInstance(tag.getProject()).createXmlAttribute(qname, namespace);
    if (anchor == null) {
      tag.add(attribute);
    }
    else {
      tag.addAfter(attribute, anchor);
    }

    CodeStyleManager.getInstance(tag.getProject()).reformat(tag);
  }
}
