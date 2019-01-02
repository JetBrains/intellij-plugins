package com.intellij.tapestry.core.util;

import org.apache.velocity.Template;
import org.apache.velocity.VelocityContext;
import org.apache.velocity.runtime.RuntimeInstance;

import java.io.StringWriter;
import java.util.Map;

/**
 * Processes velocity templates.
 */
public abstract class VelocityProcessor {

    /**
     * Processes a velocity template from the classpath.
     *
     * @param templatePath the path to the template.
     * @param context      the context.
     * @return the processed template.
     * @throws RuntimeException if an error occurs processing the template.
     */
    public static String processClasspathTemplate(String templatePath, Map<String, Object> context) throws RuntimeException {
        try {
            RuntimeInstance ri = new RuntimeInstance();
            ri.init();

            Template template = new Template();
            template.setRuntimeServices(ri);

            template.setName(templatePath);

            template.setResourceLoader(new PluginClasspathResourceLoader());
            template.setEncoding("UTF-8");

            template.process();

            VelocityContext velocityContext = new VelocityContext(context);

            StringWriter text = new StringWriter();
            template.merge(velocityContext, text);

            return text.toString();
        } catch (Exception ex) {
            throw new RuntimeException(ex);
        }
    }
}
