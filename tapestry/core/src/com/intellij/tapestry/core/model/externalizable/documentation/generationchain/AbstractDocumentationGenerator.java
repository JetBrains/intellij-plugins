package com.intellij.tapestry.core.model.externalizable.documentation.generationchain;

import com.intellij.openapi.diagnostic.Logger;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;

import java.net.URL;
import java.util.HashMap;
import java.util.Map;

/**
 * Base class for all documentation generator commands.
 */
public abstract class AbstractDocumentationGenerator implements Command {

    private static final Logger _logger = Logger.getInstance(AbstractDocumentationGenerator.class);

    private static final String BASE_PATH = "/documentation/";
    private static final URL LOGO = AbstractDocumentationGenerator.class.getResource("/com/intellij/tapestry/core/icons/g5004.png");
    private static final URL STYLE = AbstractDocumentationGenerator.class.getResource("/documentation/style.css");

    @Override
    public boolean execute(Context context) throws Exception {
        return context instanceof DocumentationGenerationContext;
    }

    public URL getDocumentationURL(String library, String middlePath, String name) {
        return getClass().getResource(BASE_PATH + library + "/" + middlePath + "/" + name + ".xml");
    }

    Map<String, Object> buildVelocityContext() {
        Map<String, Object> velocityContext = new HashMap<>();

        try {
            velocityContext.put("style", STYLE);
            velocityContext.put("logo", LOGO);
        } catch (Exception ex) {
            _logger.error(ex);
        }

        return velocityContext;
    }
}
