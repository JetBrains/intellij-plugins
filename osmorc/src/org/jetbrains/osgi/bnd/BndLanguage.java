// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
package org.jetbrains.osgi.bnd;

import com.intellij.lang.Language;

public class BndLanguage extends Language {
    public static final BndLanguage INSTANCE = new BndLanguage();

    private BndLanguage() {
        super("bnd");
    }
}
