package com.intellij.tapestry.core.model.externalizable.documentation.generationchain;

import com.intellij.tapestry.core.model.externalizable.documentation.wrapper.PresentationElementDocumentationWrapper;
import com.intellij.tapestry.core.model.presentation.Page;
import com.intellij.tapestry.core.util.VelocityProcessor;
import org.apache.commons.chain.Context;

import java.util.Map;

/**
 * Generates documentation for pages.
 */
public class PageDocumentationGenerator extends PresentationElementDocumentationGenerator {

    private static final String ICON = "/com/intellij/tapestry/core/icons/page.png";

    @Override
    public boolean execute(Context context) throws Exception {
        if (!super.execute(context))
            return false;

        if (!(getElement() instanceof Page))
            return false;

        Page page = (Page) getElement();

        Map<String, Object> velocityContext = buildVelocityContext();

        velocityContext.put("element", page);
        velocityContext.put("icon", getClass().getResource(ICON));

        try {
            velocityContext.put("documentation", new PresentationElementDocumentationWrapper(getDocumentationURL(page.getLibrary().getId(), "pages", page.getName())));
        } catch (Exception ex) {
            velocityContext.put("documentation", new PresentationElementDocumentationWrapper());
        }

        getContext().setResult(VelocityProcessor.processClasspathTemplate(VELOCITY_TEMPLATE, velocityContext));

        return true;
    }
}
