package com.intellij.tapestry.core.model.externalizable.toclasschain;

import com.intellij.tapestry.core.TapestryConstants;
import com.intellij.tapestry.core.java.IJavaField;
import com.intellij.tapestry.core.model.presentation.Mixin;
import com.intellij.tapestry.core.util.PathUtils;
import org.apache.commons.chain.Context;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Externalizes a mixin to be included in a class.
 */
public class MixinExternalizer extends ToClassExternalizer {

    @Override
    public boolean execute(Context context) throws Exception {
        if (!super.execute(context))
            return false;

        if (!(getContext().getElement() instanceof Mixin))
            return false;

        Mixin mixin = (Mixin) getContext().getElement();

        IJavaField field = mixin.getProject().getJavaTypeCreator().createField(PathUtils.getLastPathElement(mixin.getName()), mixin.getElementClass(), true, true);
        String suggestedFieldName = suggestName(field.getName(), new ArrayList<>(getContext().getTargetClass().getFields(false).keySet()));
        if (!suggestedFieldName.equals(field.getName())) {
            field = mixin.getProject().getJavaTypeCreator().createField(suggestedFieldName, mixin.getElementClass(), true, true);
        }

        mixin.getProject().getJavaTypeCreator().createFieldAnnotation(field, TapestryConstants.MIXIN_ANNOTATION, new HashMap<>());

        String serialized = field.getStringRepresentation();
        if (mixin.getProject().getJavaTypeCreator().ensureClassImport(getContext().getTargetClass(), mixin.getElementClass())) {
            serialized = serialized.replace(mixin.getElementClass().getFullyQualifiedName(), mixin.getElementClass().getName());
        }

        if (mixin.getProject().getJavaTypeCreator().ensureClassImport(getContext().getTargetClass(), mixin.getProject().getJavaTypeFinder().findType(TapestryConstants.MIXIN_ANNOTATION, true))) {
            serialized = serialized.replace(TapestryConstants.MIXIN_ANNOTATION, "Mixin");
        }

        getContext().setResult("\n" + serialized + "\n");

        return true;
    }
}
