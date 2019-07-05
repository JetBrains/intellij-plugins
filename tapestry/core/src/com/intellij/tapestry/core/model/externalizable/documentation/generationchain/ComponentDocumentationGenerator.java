package com.intellij.tapestry.core.model.externalizable.documentation.generationchain;

import com.intellij.tapestry.core.model.externalizable.documentation.wrapper.PresentationElementDocumentationWrapper;
import com.intellij.tapestry.core.model.presentation.TapestryComponent;
import com.intellij.tapestry.core.util.VelocityProcessor;
import org.apache.commons.chain.Context;

import java.util.Map;

/**
 * Generates documentation for components.
 */
public class ComponentDocumentationGenerator extends PresentationElementDocumentationGenerator {

    private static final String ICON = "/com/intellij/tapestry/core/icons/component.png";

    @Override
    public boolean execute(Context context) throws Exception {
        if (!super.execute(context))
            return false;

        if (!(getElement() instanceof TapestryComponent))
            return false;

        TapestryComponent component = (TapestryComponent) getElement();

        Map<String, Object> velocityContext = buildVelocityContext();

        velocityContext.put("element", component);
        velocityContext.put("icon", getClass().getResource(ICON));

        try {
            velocityContext.put("documentation", new PresentationElementDocumentationWrapper(getDocumentationURL(component.getLibrary().getId(), "components", component.getName())));
        } catch (Exception ex) {
            velocityContext.put("documentation", new PresentationElementDocumentationWrapper());
        }

        getContext().setResult(VelocityProcessor.processClasspathTemplate(VELOCITY_TEMPLATE, velocityContext));

        return true;
    }
}
