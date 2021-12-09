package com.intellij.tapestry.core.java.coercion;

import com.intellij.openapi.diagnostic.Logger;
import com.intellij.openapi.progress.ProcessCanceledException;
import com.intellij.tapestry.core.TapestryProject;
import com.intellij.tapestry.core.java.AssignableToAll;
import com.intellij.tapestry.core.java.IJavaType;
import org.apache.commons.chain.Chain;
import org.apache.commons.chain.Context;
import org.apache.commons.chain.impl.ChainBase;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;

/**
 * Tries to validate if a type coercion is a valid one.
 */
public final class TypeCoercionValidator {

    private static final Logger _logger = Logger.getInstance(TypeCoercionValidator.class);

    private static final Chain _chain = new ChainBase();

    static {
        _chain.addCommand(new JavaClassTypeCoercionValidator());
        _chain.addCommand(new JavaArrayTypeCoercionValidator());
        _chain.addCommand(new JavaPrimitiveTypeCoercionValidator());
        _chain.addCommand(new EnumTypeCoercionValidator());
    }

    public static boolean canCoerce(@NotNull TapestryProject project,
    								@NotNull IJavaType sourceType,
    								@Nullable String sourceValue,
    								@Nullable IJavaType targetType) {
        if (targetType == null)
            return false;

        if (sourceType instanceof AssignableToAll || targetType.isAssignableFrom(sourceType))
            return true;

        CoercionContext context = new CoercionContext(project, sourceType, sourceValue, targetType);

        try {
            if (!_chain.execute(context))
                return false;
        }
        catch (ProcessCanceledException e) {
            throw e;
        }
        catch (Exception ex) {
            _logger.error(ex);
            return false;
        }
        return context.canCoerce();
    }//canCoerce
}

@SuppressWarnings("unchecked")
class CoercionContext extends HashMap implements Context {

	private static final long serialVersionUID = -185552759258249364L;

	private static final String PROJECT_KEY 		= "project";
    private static final String SOURCE_KEY 			= "source";
    private static final String SOURCE_VALUE_KEY 	= "source-value";
    private static final String TARGET_KEY 			= "target";
    private static final String RESULT_KEY 			= "result";

    CoercionContext(TapestryProject project, IJavaType sourceType, String sourceValue, IJavaType targetType) {

        put(PROJECT_KEY, project);
        put(SOURCE_KEY, sourceType);
        put(SOURCE_VALUE_KEY, sourceValue);
        put(TARGET_KEY, targetType);
    }

    public TapestryProject getProject() {
        return (TapestryProject) get(PROJECT_KEY);
    }//getProject

    public IJavaType getSourceType() {
        return (IJavaType) get(SOURCE_KEY);
    }//getSourceType

    public String getSourceValue() {
        return (String) get(SOURCE_VALUE_KEY);
    }//getSourceValue

    public IJavaType getTargetType() {
        return (IJavaType) get(TARGET_KEY);
    }//getTargetType

    public void setResult(boolean result) {
        put(RESULT_KEY, result);
    }//setResult

    public boolean canCoerce() {
        return get(RESULT_KEY) != null && (Boolean) get(RESULT_KEY);
    }//canCoerce

}//CoercionContext
