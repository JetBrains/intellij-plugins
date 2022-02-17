package com.intellij.tapestry.core.model.externalizable.toclasschain;

import org.apache.commons.chain.Command;
import org.apache.commons.chain.Context;

import java.util.List;

/**
 * Base class for all to class externalizers.
 */
public abstract class ToClassExternalizer implements Command {

    private ExternalizeToClassContext _context;

    @Override
    public boolean execute(Context context) throws Exception {
        if (!(context instanceof ExternalizeToClassContext))
            return false;

        _context = (ExternalizeToClassContext) context;

        return true;
    }

    ExternalizeToClassContext getContext() {
        return _context;
    }

    /**
     * Suggests a name for a field.
     * If the desired name isn't taken then use that. If it it then add a number suffix to the name and increment it
     * until it doesn't conflict with an existing name.
     *
     * @param desiredName the desired name.
     * @param takenNames  the already taken names.
     * @return a name sugestion.
     */
    String suggestName(String desiredName, List<String> takenNames) {
        for (String taken : takenNames)
            if (desiredName.equals(taken)) {
                return suggestNameIterator(desiredName, desiredName + "1", takenNames);
            }

        return desiredName;
    }

    private static String suggestNameIterator(String desiredName, String newName, List<String> takenNames) {
        for (String taken : takenNames)
            if (newName.equals(taken)) {
                short incrementor = Short.parseShort(newName.substring(desiredName.length()));
                incrementor++;

                return suggestNameIterator(desiredName, newName.substring(0, desiredName.length()) + incrementor, takenNames);
            }

        return newName;
    }
}
