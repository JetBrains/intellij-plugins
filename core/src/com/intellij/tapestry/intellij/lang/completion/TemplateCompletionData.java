package com.intellij.tapestry.intellij.lang.completion;

import com.intellij.codeInsight.completion.*;
import com.intellij.codeInsight.lookup.LookupItem;
import com.intellij.codeInsight.lookup.LookupElement;
import com.intellij.openapi.editor.Document;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.editor.ScrollType;
import com.intellij.openapi.fileTypes.StdFileTypes;
import com.intellij.openapi.module.Module;
import com.intellij.openapi.roots.ProjectRootManager;
import com.intellij.psi.PsiDocumentManager;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.filters.NotFilter;
import com.intellij.psi.filters.TextFilter;
import com.intellij.psi.filters.position.LeftNeighbour;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.psi.xml.XmlAttribute;
import com.intellij.psi.xml.XmlTag;
import com.intellij.util.text.CharArrayUtil;
import com.intellij.xml.util.HtmlUtil;
import com.intellij.tapestry.core.exceptions.NotFoundException;
import com.intellij.tapestry.core.model.presentation.Component;
import com.intellij.tapestry.core.model.presentation.TapestryParameter;
import com.intellij.tapestry.intellij.util.TapestryUtils;

import java.util.Set;

/**
 * Provides auto-completion for Tapestry components.
 */
public class TemplateCompletionData extends HtmlCompletionData {

    public TemplateCompletionData() {
        super();

        LeftNeighbour left = new LeftNeighbour(new TextFilter("."));
        CompletionVariant completionVariant = new CompletionVariant(new NotFilter(left));
        completionVariant.includeScopeClass(com.intellij.psi.impl.source.tree.LeafPsiElement.class, true);
        completionVariant.addCompletionFilter(createTagCompletionFilter());
        completionVariant.addCompletion(new ComponentNameContextGetter());
        completionVariant.addCompletion(new ParameterValueContextGetter());
        completionVariant.setInsertHandler(new DefaultInsertHandler());
        registerVariant(completionVariant);
    }

    /**
     * {@inheritDoc}
     */
    public void completeReference(PsiReference psiReference, Set<LookupElement> set, CompletionContext completionContext, PsiElement psiElement) {
        Module module = ProjectRootManager.getInstance(completionContext.project).getFileIndex().getModuleForFile(psiElement.getContainingFile().getOriginalFile().getVirtualFile());

        // if this isn't a Tapestry module don't do anything
        if (!TapestryUtils.isTapestryModule(module))
            return;

        super.completeReference(psiReference, set, psiElement, psiElement.getContainingFile().getOriginalFile(), 0);

        PsiElement element = psiReference.getElement();

        // Completion of tag attributes
        if (element instanceof XmlAttribute) {
            XmlTag tag = PsiTreeUtil.getParentOfType(psiElement, XmlTag.class);
          Component component = TapestryUtils.getComponentFromTag(module, tag);
          if(component == null) return;

            for (TapestryParameter parameter : component.getParameters().values()) {
                LookupItem variant = new LookupItem(parameter.getName(), parameter.getName());
                variant.setInsertHandler(new XmlAttributeInsertHandler());
                set.add(variant);
            }
        }
    }

    private class XmlAttributeInsertHandler extends BasicInsertHandler<LookupElement> {

        @Override
        public void handleInsert(InsertionContext insertionContext, LookupElement lookupitem) {
            super.handleInsert(insertionContext, lookupitem);

            Editor editor = insertionContext.getEditor();
            Document document = editor.getDocument();
            int j = editor.getCaretModel().getOffset();

            if (PsiDocumentManager.getInstance(editor.getProject()).getPsiFile(document).getFileType() == StdFileTypes.HTML && HtmlUtil.isSingleHtmlAttribute((String) lookupitem.getObject())) {
                return;
            }

            CharSequence charsequence = document.getCharsSequence();

            if (!CharArrayUtil.regionMatches(charsequence, j, "=\"") && !CharArrayUtil.regionMatches(charsequence, j, "='")) {
                if ("/> \n\t\r".indexOf(document.getCharsSequence().charAt(j)) < 0) {
                    document.insertString(j, "=\"\" ");
                } else {
                    document.insertString(j, "=\"\"");
                }
            }
            editor.getCaretModel().moveToOffset(j + 2);
            editor.getScrollingModel().scrollToCaret(ScrollType.RELATIVE);
            editor.getSelectionModel().removeSelection();
        }

        public XmlAttributeInsertHandler() {
        }
    }
}
