package com.intellij.tapestry.core.java.coercion;

import com.intellij.tapestry.core.java.IJavaPrimitiveType;
import com.intellij.tapestry.core.java.IJavaType;
import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;

import java.util.HashMap;
import java.util.Map;

public class JavaPrimitiveTypeCoercionValidator implements Command {

    private static final Map<String, String> PRIMITIVE_COERCION_MAP = new HashMap<>();

    static {
        PRIMITIVE_COERCION_MAP.put("byte", "java.lang.Byte");
        PRIMITIVE_COERCION_MAP.put("short", "java.lang.Short");
        PRIMITIVE_COERCION_MAP.put("int", "java.lang.Integer");
        PRIMITIVE_COERCION_MAP.put("long", "java.lang.Long");
        PRIMITIVE_COERCION_MAP.put("float", "java.lang.Float");
        PRIMITIVE_COERCION_MAP.put("double", "java.lang.Double");
        PRIMITIVE_COERCION_MAP.put("char", "java.lang.Character");
        PRIMITIVE_COERCION_MAP.put("boolean", "java.lang.Boolean");
    }

    @Override
    public boolean execute(Context context) throws Exception {
        if (!(((CoercionContext) context).getSourceType() instanceof IJavaPrimitiveType || ((CoercionContext) context).getTargetType() instanceof IJavaPrimitiveType))
            return false;

        IJavaType sourceType = ((CoercionContext) context).getSourceType();
        IJavaType targetType = ((CoercionContext) context).getTargetType();

        if (sourceType instanceof IJavaPrimitiveType) {
            if (PRIMITIVE_COERCION_MAP.containsKey(sourceType.getName()))
                sourceType = ((CoercionContext) context).getProject().getJavaTypeFinder().findType(PRIMITIVE_COERCION_MAP.get(sourceType.getName()), true);
        }

        if (targetType instanceof IJavaPrimitiveType) {
            if (PRIMITIVE_COERCION_MAP.containsKey(targetType.getName()))
                targetType = ((CoercionContext) context).getProject().getJavaTypeFinder().findType(PRIMITIVE_COERCION_MAP.get(targetType.getName()), true);
        }

        ((CoercionContext) context).setResult(TypeCoercionValidator.canCoerce(((CoercionContext) context).getProject(), sourceType, ((CoercionContext) context).getSourceValue(), targetType));

        return true;
    }
}
