package com.intellij.tapestry.core.model.externalizable.documentation.generationchain;

import com.intellij.tapestry.core.model.externalizable.documentation.wrapper.PresentationElementDocumentationWrapper;
import com.intellij.tapestry.core.model.presentation.Mixin;
import com.intellij.tapestry.core.util.VelocityProcessor;
import org.apache.commons.chain.Context;

import java.util.Map;

/**
 * Generates documentation for mixins.
 */
public class MixinDocumentationGenerator extends PresentationElementDocumentationGenerator {

    private static final String ICON = "/com/intellij/tapestry/core/icons/mixin.png";

    @Override
    public boolean execute(Context context) throws Exception {
        if (!super.execute(context))
            return false;

        if (!(getElement() instanceof Mixin))
            return false;

        Mixin mixin = (Mixin) getElement();

        Map<String, Object> velocityContext = buildVelocityContext();

        velocityContext.put("element", mixin);
        velocityContext.put("icon", getClass().getResource(ICON));

        try {
            velocityContext.put("documentation", new PresentationElementDocumentationWrapper(getDocumentationURL(mixin.getLibrary().getId(), "mixins", mixin.getName())));
        } catch (Exception ex) {
            velocityContext.put("documentation", new PresentationElementDocumentationWrapper());
        }

        getContext().setResult(VelocityProcessor.processClasspathTemplate(VELOCITY_TEMPLATE, velocityContext));

        return true;
    }
}
