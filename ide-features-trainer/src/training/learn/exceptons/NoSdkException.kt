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
