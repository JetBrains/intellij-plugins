package com.intellij.tapestry.core.model.externalizable.documentation.generationchain;

import com.intellij.tapestry.core.log.Logger;
import com.intellij.tapestry.core.log.LoggerFactory;
import org.apache.commons.chain.impl.ChainBase;

/**
 * The starting point of the documentation generation chain.
 */
public class DocumentationGenerationChain extends ChainBase {

    private static final Logger _logger = LoggerFactory.getInstance().getLogger(DocumentationGenerationChain.class);

    private static final DocumentationGenerationChain _me = new DocumentationGenerationChain();

    private DocumentationGenerationChain() {
        super();

        addCommand(new ComponentDocumentationGenerator());
        addCommand(new PageDocumentationGenerator());
        addCommand(new MixinDocumentationGenerator());
        addCommand(new HomeDocumentationGenerator());
    }

    public static DocumentationGenerationChain getInstance() {
        return _me;
    }

    /**
     * Generates the documentation.
     *
     * @param element the element to generate the documentation for.
     * @return the generated documentation.
     * @throws Exception if an error occurs generating the documentation.
     */
    public String generate(Object element) throws Exception {
        DocumentationGenerationContext context = new DocumentationGenerationContext(element);

        try {
            execute(context);
        } catch (Exception ex) {
            _logger.error(ex);

            throw ex;
        }

        return context.getResult();
    }
}
