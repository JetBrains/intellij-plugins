package com.intellij.tapestry.core.model.externalizable.totemplatechain;

import com.intellij.tapestry.core.TapestryProject;
import com.intellij.tapestry.core.model.presentation.Page;
import org.apache.commons.chain.Context;

/**
 * Externalizes a page to be included in a template.
 */
public class PageExternalizer extends ToTemplateExternalizer {

    @Override
    public boolean execute(Context context) throws Exception {
        if (!super.execute(context))
            return false;

        if (!(getContext().getElement() instanceof Page))
            return false;

        Page page = (Page) getContext().getElement();

        if (page.getElementClass().getFile() == null) {
            throw new RuntimeException("The page is invalid!!");
        }

        String pageName;
        if (page.getLibrary().getId().equals(TapestryProject.APPLICATION_LIBRARY_ID) || page.getLibrary().getId().equals(TapestryProject.CORE_LIBRARY_ID)) {
            pageName = page.getName();
        } else {
            pageName = page.getLibrary().getId() + "/" + page.getName();
        }

        StringBuilder serialized = new StringBuilder();

        serialized.append("<").append(getContext().getNamespacePrefix()).append(":pagelink");
        serialized.append(" page=\"").append(pageName).append("\">");
        serialized.append("Link to ").append(pageName);
        serialized.append("</").append(getContext().getNamespacePrefix()).append(":pagelink>");

        getContext().setResult(serialized.toString());

        return true;
    }
}
