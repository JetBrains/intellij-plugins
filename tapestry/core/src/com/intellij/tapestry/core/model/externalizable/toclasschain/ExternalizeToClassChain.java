package com.intellij.tapestry.core.model.externalizable.toclasschain;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.tapestry.core.java.IJavaClassType;
import org.apache.commons.chain.impl.ChainBase;

/**
 * The starting point of the externalize to class chain.
 */
public final class ExternalizeToClassChain extends ChainBase {

    private static final Logger _logger = Logger.getInstance(ExternalizeToClassChain.class);

    private static final ExternalizeToClassChain _me = new ExternalizeToClassChain();

    private ExternalizeToClassChain() {
        super();

        addCommand(new ComponentExternalizer());
        addCommand(new PageExternalizer());
        addCommand(new MixinExternalizer());
    }

    public static ExternalizeToClassChain getInstance() {
        return _me;
    }

    /**
     * Generates a representation of this element .
     *
     * @param element     the element to generate the representation for.
     * @param targetClass the class where the representation is going to be included.
     * @return the the element representation to be included in a class.
     * @throws Exception if an error occurs.
     */
    public String externalize(Object element, IJavaClassType targetClass) throws Exception {
        ExternalizeToClassContext context = new ExternalizeToClassContext(element, targetClass);

        try {
            execute(context);
        } catch (Exception ex) {
            _logger.error(ex);

            throw ex;
        }

        return context.getResult();
    }
}
