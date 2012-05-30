package com.intellij.tapestry.core.java.coercion;

import com.intellij.psi.CommonClassNames;
import com.intellij.tapestry.core.java.IJavaArrayType;
import com.intellij.tapestry.core.java.IJavaClassType;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;

public class JavaArrayTypeCoercionValidator implements Command {

    public boolean execute(Context context) throws Exception {

        // if none of the types are an array don't treat this case
        if (!(((CoercionContext) context).getSourceType() instanceof IJavaArrayType))
            return false;

        // if the target type is a subtype os java.util.List then coerce
        if (((CoercionContext) context).getTargetType().isAssignableFrom(((CoercionContext) context).getProject().getJavaTypeFinder().findType(
          CommonClassNames.JAVA_UTIL_LIST, true))) {
            ((CoercionContext) context).setResult(true);

            return true;
        }

        // if the target type is a boolean then coerce
        if (((CoercionContext) context).getTargetType().isAssignableFrom(((CoercionContext) context).getProject().getJavaTypeFinder().findType("java.lang.Boolean", true))) {
            ((CoercionContext) context).setResult(true);

            return true;
        }

        // if the target type is a GridDataSource then coerce
        if ((((CoercionContext) context).getTargetType() instanceof IJavaClassType && ((IJavaClassType) (((CoercionContext) context).getTargetType())).getFullyQualifiedName().equals("org.apache.tapestry5.grid.GridDataSource"))) {
            ((CoercionContext) context).setResult(true);

            return true;
        }

        return true;
    }
}
