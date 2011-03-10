package com.intellij.flex;

import flex2.compiler.SourcePath;
import flex2.compiler.common.SinglePathResolver;
import flex2.compiler.io.LocalFile;
import flex2.compiler.io.VirtualFile;

import java.io.File;

public class SourcePathResolver implements SinglePathResolver {
    private SourcePath mySourcePath;

    public SourcePathResolver(final SourcePath sourcePath) {
        mySourcePath = sourcePath;
    }

    public VirtualFile resolve(final String relative) {
        if (relative != null && relative.length() > 0) {
            for (final Object directory : mySourcePath.getPaths()) {
                final File file = new File((File) directory, relative);
                if (file.exists()) {
                    return new LocalFile(file);
                }
            }
        }
        return null;
    }
}

