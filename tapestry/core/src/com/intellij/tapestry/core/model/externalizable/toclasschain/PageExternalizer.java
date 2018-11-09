package com.intellij.tapestry.core.model.externalizable.toclasschain;

import com.intellij.tapestry.core.TapestryConstants;
import com.intellij.tapestry.core.java.IJavaField;
import com.intellij.tapestry.core.model.presentation.Page;
import com.intellij.tapestry.core.util.PathUtils;
import org.apache.commons.chain.Context;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Externalizes a page to be included in a class.
 */
public class PageExternalizer extends ToClassExternalizer {

    @Override
    public boolean execute(Context context) throws Exception {
        if (!super.execute(context))
            return false;

        if (!(getContext().getElement() instanceof Page))
            return false;

        Page page = (Page) getContext().getElement();

        IJavaField field = page.getProject().getJavaTypeCreator().createField(PathUtils.getLastPathElement(page.getName()), page.getElementClass(), true, true);
        String suggestedFieldName = suggestName(field.getName(), new ArrayList<>(getContext().getTargetClass().getFields(false).keySet()));
        if (!suggestedFieldName.equals(field.getName())) {
            field = page.getProject().getJavaTypeCreator().createField(suggestedFieldName, page.getElementClass(), true, true);
        }

        page.getProject().getJavaTypeCreator().createFieldAnnotation(field, TapestryConstants.INJECT_PAGE_ANNOTATION, new HashMap<>());

        String serialized = field.getStringRepresentation();
        if (page.getProject().getJavaTypeCreator().ensureClassImport(getContext().getTargetClass(), page.getElementClass())) {
            serialized = serialized.replace(page.getElementClass().getFullyQualifiedName(), page.getElementClass().getName());
        }

        if (page.getProject().getJavaTypeCreator().ensureClassImport(getContext().getTargetClass(), page.getProject().getJavaTypeFinder().findType(TapestryConstants.INJECT_PAGE_ANNOTATION, true))) {
            serialized = serialized.replace(TapestryConstants.INJECT_PAGE_ANNOTATION, "InjectPage");
        }

        getContext().setResult("\n" + serialized + "\n");

        return true;
    }
}
