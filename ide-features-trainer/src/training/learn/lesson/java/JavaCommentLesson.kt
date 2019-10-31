/*
 * Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */
package training.learn.lesson.java

import com.intellij.psi.JavaTokenType
import com.intellij.psi.tree.IElementType
import training.lang.JavaLangSupport
import training.learn.interfaces.Module
import training.learn.lesson.general.SingleLineCommentLesson
import training.learn.lesson.kimpl.LessonSample
import training.learn.lesson.kimpl.parseLessonSample

class JavaCommentLesson(module: Module) : SingleLineCommentLesson(module, JavaLangSupport.lang) {
  override val commentElementType: IElementType
    get() = JavaTokenType.END_OF_LINE_COMMENT
  override val sample: LessonSample
    get() = parseLessonSample("""import java.awt.Color;

class CommentDemo {
    public static void main() {

        float hue = 5;
        float saturation = 10;
        float brightness = 10;

        int rgb = Color.HSBtoRGB(hue, saturation, brightness);
        <caret>int red = (rgb >> 16) &0xFF;
        int green = (rgb >> 8) &0xFF;
        int blue = rgb &0xFF;

    }
}""")
}