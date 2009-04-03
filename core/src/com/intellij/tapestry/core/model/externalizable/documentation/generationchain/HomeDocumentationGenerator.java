package com.intellij.tapestry.core.model.externalizable.documentation.generationchain;

import com.intellij.tapestry.core.model.externalizable.documentation.Home;
import com.intellij.tapestry.core.model.externalizable.documentation.wrapper.PresentationElementDocumentationWrapper;
import com.intellij.tapestry.core.util.ClassLocator;
import com.intellij.tapestry.core.util.StringUtils;
import com.intellij.tapestry.core.util.VelocityProcessor;
import org.apache.commons.chain.Context;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Generates the documentation home page.
 */
public class HomeDocumentationGenerator extends AbstractDocumentationGenerator {

    private static final String VELOCITY_TEMPLATE = "/documentation/home.vm";
    private static final String[] DOCUMENTATION_ELEMENT_TYPES = new String[]{"components", "pages", "mixins"};

    public boolean execute(Context context) throws Exception {
        if (!super.execute(context))
            return false;

        DocumentationGenerationContext myContext = (DocumentationGenerationContext) context;
        if (!(myContext.getElement() instanceof Home))
            return false;

        Home element = (Home) myContext.getElement();

        Map<String, Object> velocityContext = buildVelocityContext();

        // Tapestry module names
        velocityContext.put("modules", element.getTapestryProjects());

        for (String elementType : DOCUMENTATION_ELEMENT_TYPES) {
            Map<String, String> filesProperties = new HashMap<String, String>();

            List<ClassLocator.ClassLocation> resources;
            try {
                ClassLocator locator = new ClassLocator(getClass().getClassLoader(), "documentation.core." + elementType);
                resources = locator.getAllClassLocations();
            } catch (Exception ex) {
                throw new RuntimeException(ex);
            }

            if (resources != null) {
                for (ClassLocator.ClassLocation resource : resources) {
                    // we are only interested in .properties files
                    if (resource.getUrl().toExternalForm().endsWith(".xml")) {
                        PresentationElementDocumentationWrapper elementDocumentation = new PresentationElementDocumentationWrapper(resource.getUrl());

                        filesProperties.put(resource.getClassName(), StringUtils.truncateWords(elementDocumentation.getDescription(), 100) + "(...)");
                    }
                }

                velocityContext.put(elementType, filesProperties);
            }
        }

        myContext.setResult(VelocityProcessor.processClasspathTemplate(VELOCITY_TEMPLATE, velocityContext));

        return true;
    }
}
