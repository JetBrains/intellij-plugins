// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package org.nanocontainer.script;

import org.picocontainer.PicoException;

/**
 * Indicates that a given file extension has no corresponding builder.  The
 * message will also indicate all supported builders.
 * @author Michael Rimov
 * @version 1.0
 */
public class UnsupportedScriptTypeException extends PicoException {

    private final String specifiedFileExtension;

    private final String[] allSupportedFileExtensions;

    public UnsupportedScriptTypeException(String specifiedFileExtension, String[] allSupportedFileExtensions) {
        super();
        this.specifiedFileExtension = specifiedFileExtension;
        this.allSupportedFileExtensions = allSupportedFileExtensions;
    }



    /**
     * Transforms the constructor arguments into a real exption
     * @param specifiedFileExtension String
     * @param allSupportedFileExtensions String[]
     * @return String
     */
    private  String buildExceptionMessage() {
        StringBuffer message = new StringBuffer(48);
        message.append("Unsupported file extension '");
        message.append(specifiedFileExtension);
        message.append("'.  Supported extensions are: [");

        if (allSupportedFileExtensions != null) {
            boolean needPipe = false;
            for (int i = 0; i < allSupportedFileExtensions.length; i++) {
                if (needPipe) {
                    message.append("|");
                } else {
                    needPipe = true;
                }

                message.append(allSupportedFileExtensions[i]);
            }

            message.append("].");
        } else {
            message.append(" null ");
        }

        return message.toString();
    }

    @Override
    public String getMessage() {
        return buildExceptionMessage();
    }

    public String[] getSystemSupportedExtensions() {
        return allSupportedFileExtensions;
    }

    public String getRequestedExtension() {
        return specifiedFileExtension;
    }

}
