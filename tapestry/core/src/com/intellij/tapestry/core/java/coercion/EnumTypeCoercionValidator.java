package com.intellij.tapestry.core.java.coercion;

import com.intellij.openapi.util.text.StringUtil;
import com.intellij.tapestry.core.java.IJavaClassType;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;

public class EnumTypeCoercionValidator implements Command {
    @Override
    public boolean execute(Context context) throws Exception {
        // if the target type is not an enum don't treat this case
        if (!(((CoercionContext) context).getTargetType() instanceof IJavaClassType) || !((IJavaClassType) ((CoercionContext) context).getTargetType()).isEnum())
            return false;

        // this validator is only to coerce strings to enums
        if (!((IJavaClassType) ((CoercionContext) context).getSourceType()).getFullyQualifiedName().equals("java.lang.String"))
            return false;

        for (String fieldName : ((IJavaClassType) ((CoercionContext) context).getTargetType()).getFields(true).keySet()) {
            if (((CoercionContext) context).getSourceValue() != null && StringUtil.toLowerCase(fieldName)
              .equals(StringUtil.toLowerCase(((CoercionContext) context).getSourceValue()))) {
                ((CoercionContext) context).setResult(true);

                return true;
            }
        }

        return false;
    }
}
