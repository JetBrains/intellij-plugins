package com.intellij.tapestry.core.model.externalizable.documentation.generationchain;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.tapestry.core.model.presentation.PresentationLibraryElement;
import org.apache.commons.chain.Context;

/**
 * Base class for all presentation elements documentation generator commands.
 */
public abstract class PresentationElementDocumentationGenerator extends AbstractDocumentationGenerator {

    static final String VELOCITY_TEMPLATE = "/documentation/presentation-element.vm";

    private static final Logger _logger = Logger.getInstance(PresentationElementDocumentationGenerator.class);

    private DocumentationGenerationContext _context;
    private PresentationLibraryElement _element;

    @Override
    public boolean execute(Context context) throws Exception {
        if (!super.execute(context))
            return false;

        _context = (DocumentationGenerationContext) context;
        if (!(_context.getElement() instanceof PresentationLibraryElement))
            return false;

        _element = (PresentationLibraryElement) _context.getElement();

        if (_element.getElementClass().getFile() == null)
            _logger.error("Couldn't find file for class \"" + _element.getElementClass().getFullyQualifiedName() + "\"");

        return true;
    }


    DocumentationGenerationContext getContext() {
        return _context;
    }

    PresentationLibraryElement getElement() {
        return _element;
    }
}
