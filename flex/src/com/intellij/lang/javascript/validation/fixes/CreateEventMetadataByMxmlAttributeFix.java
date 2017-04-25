package com.intellij.lang.javascript.validation.fixes;

import com.intellij.codeInsight.FileModificationService;
import com.intellij.codeInsight.template.Template;
import com.intellij.codeInsight.template.TemplateManager;
import com.intellij.javascript.flex.FlexPredefinedTagNames;
import com.intellij.javascript.flex.mxml.FlexCommonTypeNames;
import com.intellij.javascript.flex.mxml.MxmlJSClass;
import com.intellij.lang.javascript.JSBundle;
import com.intellij.lang.javascript.JavaScriptSupportLoader;
import com.intellij.lang.javascript.psi.JSReferenceExpression;
import com.intellij.lang.javascript.psi.ecmal4.XmlBackedJSClassFactory;
import com.intellij.openapi.application.WriteAction;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiFile;
import com.intellij.psi.XmlElementFactory;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlFile;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.IncorrectOperationException;
import com.intellij.xml.XmlElementDescriptor;
import org.jetbrains.annotations.NotNull;

public class CreateEventMetadataByMxmlAttributeFix extends BaseCreateFix {
  private final String myEventName;

  public CreateEventMetadataByMxmlAttributeFix(final String eventName) {
    myEventName = eventName;
  }

  @NotNull
  public String getName() {
    return JSBundle.message("declare.event.0", myEventName);
  }

  @Override
  protected void applyFix(final Project project, final PsiElement psiElement, PsiFile file, Editor editor) {
    assert psiElement instanceof XmlAttribute;

    final XmlTag tag = (XmlTag)psiElement.getParent();
    final XmlElementDescriptor descriptor = tag.getDescriptor();
    PsiElement type = descriptor == null ? null : descriptor.getDeclaration();

    if (type == null) {
      return; // can not resolve
    }

    if (!FileModificationService.getInstance().preparePsiElementForWrite(type)) {
      return;
    }

    file = type.getContainingFile();
    editor = getEditor(project, file);
    if (editor == null) {
      return;
    }

    final boolean addingToMxml = type instanceof XmlFile;

    final TemplateManager templateManager = TemplateManager.getInstance(project);
    final Template template = templateManager.createTemplate("", "");
    template.setToReformat(true);

    if (addingToMxml) {
      template.addTextSegment("\n");
    }
    template.addTextSegment("[Event(name=\"" + myEventName + "\", type=\"");
    template.addVariable(new MyExpression(FlexCommonTypeNames.FLASH_EVENT_FQN), true);
    template.addTextSegment("\")]");
    if (!addingToMxml) {
      template.addTextSegment("\n");
    }

    final int offset;
    if (addingToMxml) {
      XmlTag metadataTag = WriteAction.compute(() -> createOrGetMetadataTag((XmlFile)type));
      offset = metadataTag.getValue().getTextRange().getStartOffset();
    }
    else {
      offset = type.getTextRange().getStartOffset();
    }

    navigate(project, editor, offset, file.getVirtualFile());
    templateManager.startTemplate(editor, template);
  }

  protected void buildTemplate(final Template template,
                               final JSReferenceExpression referenceExpression,
                               final boolean staticContext,
                               final PsiFile file,
                               final PsiElement anchorParent) {
    assert false; // not used
  }

  private static XmlTag createOrGetMetadataTag(final XmlFile xmlFile) throws IncorrectOperationException {
    assert JavaScriptSupportLoader.isFlexMxmFile(xmlFile) : xmlFile;
    final XmlTag rootTag = XmlBackedJSClassFactory.getRootTag(xmlFile);
    final XmlTag[] metadataTags = MxmlJSClass.findLanguageSubTags(rootTag, FlexPredefinedTagNames.METADATA);

    return metadataTags.length > 0 ? metadataTags[0] : createMetadataTag(rootTag);
  }

  private static XmlTag createMetadataTag(final XmlTag rootTag) {
    String prefix = rootTag.getPrefixByNamespace(JavaScriptSupportLoader.MXML_URI3);
    if (prefix == null) prefix = rootTag.getPrefixByNamespace(JavaScriptSupportLoader.MXML_URI);
    if (prefix == null) prefix = "";

    final String qName = prefix + (prefix.isEmpty() ? "" : ":") + FlexPredefinedTagNames.METADATA;
    final XmlTag newTag = XmlElementFactory.getInstance(rootTag.getProject()).createTagFromText("<" + qName + ">\n</" + qName + ">");

    final XmlTag[] subTags = rootTag.getSubTags();
    return subTags.length > 0 && MxmlJSClass.isFxLibraryTag(subTags[0])
           ? (XmlTag)rootTag.addAfter(newTag, subTags[0])
           : rootTag.addSubTag(newTag, true);
  }
}
