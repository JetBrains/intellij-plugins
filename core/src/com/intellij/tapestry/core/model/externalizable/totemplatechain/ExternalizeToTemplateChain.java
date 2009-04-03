package com.intellij.tapestry.core.model.externalizable.totemplatechain;

import com.intellij.tapestry.core.log.Logger;
import com.intellij.tapestry.core.log.LoggerFactory;
import org.apache.commons.chain.impl.ChainBase;

/**
 * The starting point of the externalize to template chain.
 */
public class ExternalizeToTemplateChain extends ChainBase {

    private static final Logger _logger = LoggerFactory.getInstance().getLogger(ExternalizeToTemplateChain.class);

    private static final ExternalizeToTemplateChain _me = new ExternalizeToTemplateChain();

    private ExternalizeToTemplateChain() {
        super();

        addCommand(new ComponentExternalizer());
        addCommand(new PageExternalizer());
    }

    public static ExternalizeToTemplateChain getInstance() {
        return _me;
    }

    /**
     * Generates a representation of this element .
     *
     * @param element         the element to generate the representation for.
     * @param namespacePrefix the Tapestry namespace prefix in the template where the representation is going to be included.
     * @return the the element representation to be included in a template.
     * @throws Exception if an error occurs.
     */
    public String externalize(Object element, String namespacePrefix) throws Exception {
        ExternalizeToTemplateContext context = new ExternalizeToTemplateContext(element, namespacePrefix);

        try {
            execute(context);
        } catch (Exception ex) {
            _logger.error(ex);

            throw ex;
        }

        return context.getResult();
    }
}
