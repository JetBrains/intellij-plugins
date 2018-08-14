package com.intellij.tapestry.core.model.presentation.valueresolvers.property;

import com.intellij.tapestry.core.java.IJavaClassType;
import com.intellij.tapestry.core.java.IJavaType;
import com.intellij.tapestry.core.model.presentation.valueresolvers.AbstractValueResolver;
import com.intellij.tapestry.core.model.presentation.valueresolvers.ValueResolverContext;
import com.intellij.tapestry.core.model.presentation.valueresolvers.property.specialcases.*;
import org.apache.commons.chain.Chain;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;
import org.apache.commons.chain.impl.ChainBase;

import java.util.StringTokenizer;

/**
 * Resolves property values.
 */
public class PropResolver extends AbstractValueResolver {

    private static final String PREFIX = "prop";

    private static final Chain _specialCasesChain = new ChainBase(new Command[]{
            new SpecialCaseBooleanResolver(),
            new SpecialCaseNullResolver(),
            new SpecialCaseThisResolver(),
            new SpecialCaseLiteralStringResolver(),
            new SpecialCaseNumericResolver(),
            new SpecialCaseRangeIntegersResolver()});

    private static final Chain _normalCasesChain = new ChainBase(new Command[]{
            new SingleMethodResolver(),
            new SinglePropertyResolver()});

    @Override
    public boolean execute(Context context) throws Exception {
        String prefix = getPrefix(((ValueResolverContext) context).getValue(), ((ValueResolverContext) context).getDefaultPrefix());

        if (prefix == null || !prefix.equals(PREFIX))
            return false;

        // is this a special case ?
        if (_specialCasesChain.execute(context))
            return true;

        StringTokenizer tokenizer = new StringTokenizer(getCleanValue(((ValueResolverContext) context).getValue()), ".");
        IJavaType currentType = ((ValueResolverContext) context).getContextClass();
        ValueResolverContext currentContext = null;

        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken();

            if (!(currentType instanceof IJavaClassType)) {
                if (tokenizer.hasMoreTokens())
                    return true;
                else {
                    ((ValueResolverContext) context).setResultType(currentType);

                    if (currentContext != null)
                        ((ValueResolverContext) context).setResultCodeBind(currentContext.getResultCodeBind());

                    return true;
                }
            }

            currentContext = new ValueResolverContext(((ValueResolverContext) context).getProject(), (IJavaClassType) currentType, token, PREFIX);

            if (_normalCasesChain.execute(currentContext))
                currentType = currentContext.getResultType();
            else
                return false;
        }

        ((ValueResolverContext) context).setResultType(currentType);

        if (currentContext != null)
            ((ValueResolverContext) context).setResultCodeBind(currentContext.getResultCodeBind());

        return true;
    }
}
