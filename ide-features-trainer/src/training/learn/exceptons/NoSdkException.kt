/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package training.learn.exceptons

import training.lang.LangManager
import training.learn.LearnBundle

/**
* @author Sergey Karashevich
*/
class NoSdkException : InvalidSdkException {

    constructor(type: String) : super("Cannot start learning: the $type SDK is not specified") {}

    constructor() : super(LearnBundle.message("dialog.noSdk.message", LangManager.getInstance().getLanguageDisplayName()!!)) {}
}
