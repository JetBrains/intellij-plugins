/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package training.util;

public class XmlModuleConstants {
    public final static String MODULE_ALLMODULE_FILENAME = "modules.xml";
    public final static String MODULE_TYPE_ATTR = "module";
    public final static String LANGUAGE_NODE_ATTR = "language";
    public final static String LANGUAGE_NAME_ATTR = "lang";

    public final static String MODULE_NAME_ATTR = "name";
    public final static String MODULE_ID_ATTR = "id";
    public final static String MODULE_XML_LESSON_ELEMENT = "lesson";
    public final static String MODULE_KT_LESSON_ELEMENT = "lesson-kt"; // Kotlin-described lesson
    public final static String MODULE_ANSWER_PATH_ATTR = "answerPath";
    public final static String MODULE_DESCRIPTION_ATTR = "description";
    public final static String MODULE_SDK_TYPE = "sdkType";
    public final static String MODULE_FILE_TYPE = "fileType";

    /**
     * Some lessons could be unfinished or badly implemented and does not fit for release.
     * Nevertheless they are accessible from master snapshots.
     */
    public final static String MODULE_LESSON_UNFINISHED_ATTR = "unfinished";

    public final static String MODULE_LESSONS_PATH_ATTR = "lessonsPath";
    public final static String MODULE_LESSON_FILENAME_ATTR = "filename";
    public final static String MODULE_LESSON_IMPLEMENTATION_ATTR = "implementationClass";
    public final static String MODULE_LESSON_LANGUAGE_ATTR = "lang";
    public final static String MODULE_LESSON_SAMPLE_ATTR = "sample";
}
