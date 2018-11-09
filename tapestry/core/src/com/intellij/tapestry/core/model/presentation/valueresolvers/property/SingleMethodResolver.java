package com.intellij.tapestry.core.model.presentation.valueresolvers.property;

import com.intellij.tapestry.core.java.IJavaMethod;
import com.intellij.tapestry.core.model.presentation.valueresolvers.AbstractValueResolver;
import com.intellij.tapestry.core.model.presentation.valueresolvers.ValueResolverContext;
import org.apache.commons.chain.Context;

import java.util.Collection;
import java.util.regex.Pattern;

/**
 * Resolves the special case when a property value is given as a method name.
 */
public class SingleMethodResolver extends AbstractValueResolver {

    private static final Pattern PATTERN = Pattern.compile("\\w+\\(\\)");

    @Override
    public boolean execute(Context context) throws Exception {
        String cleanValue = getCleanValue(((ValueResolverContext) context).getValue());

        if (PATTERN.matcher(cleanValue).matches()) {
            Collection<IJavaMethod> candidateMethods = ((ValueResolverContext) context).getContextClass().findPublicMethods(cleanValue.substring(0, cleanValue.indexOf("()")));

            for (IJavaMethod method : candidateMethods) {
                if (method.getParameters().size() == 0) {

                    ((ValueResolverContext) context).setResultType(method.getReturnType());

                    if (method.getReturnType() != null)
                        ((ValueResolverContext) context).setResultCodeBind(method);

                    return true;
                }
            }

            return true;
        }

        return false;
    }
}
