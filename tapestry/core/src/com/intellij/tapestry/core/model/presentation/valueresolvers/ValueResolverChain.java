package com.intellij.tapestry.core.model.presentation.valueresolvers;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.tapestry.core.TapestryProject;
import com.intellij.tapestry.core.java.IJavaClassType;
import com.intellij.tapestry.core.model.presentation.valueresolvers.property.PropResolver;
import org.apache.commons.chain.impl.ChainBase;
import org.jetbrains.annotations.Nullable;

/**
 * The starting point of the resolvers chain.
 */
public final class ValueResolverChain extends ChainBase {

    private static final Logger _logger = Logger.getInstance(ValueResolverChain.class);

    private static final ValueResolverChain _me = new ValueResolverChain();

    private ValueResolverChain() {
        super();

        addCommand(new PropResolver());
        addCommand(new LiteralResolver());
        addCommand(new ComponentResolver());
        addCommand(new ValidateResolver());
        addCommand(new MessageResolver());
    }

    public static ValueResolverChain getInstance() {
        return _me;
    }

    /**
     * Resolves a value.
     *
     * @param project       the project where the value should be resolved on.
     * @param contextClass  the class that provides the context for resolving the value.
     * @param value         the value to be resolved.
     * @param defaultPrefix the default prefix of the value to be resolved.
     * @return the resolved value or {@code null} if it wasn't possible to resolve the value.
     * @throws Exception if an error occurs resolving the value.
     */
    @Nullable
    public ResolvedValue resolve(TapestryProject project, IJavaClassType contextClass, String value, String defaultPrefix) throws Exception {
        ValueResolverContext context = new ValueResolverContext(project, contextClass, value, defaultPrefix);

        try {
            execute(context);
        } catch (Exception ex) {
            if (!(ex instanceof ProcessCanceledException)) _logger.error(ex);

            throw ex;
        }

        if (context.getResultType() != null)
            return new ResolvedValue(context.getResultType(), context.getResultCodeBind());
        else
            return null;
    }
}
