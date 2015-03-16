package org.jetbrains.training.lesson;

import com.intellij.openapi.fileEditor.FileEditorManager;
import com.intellij.openapi.fileEditor.FileEditorManagerEvent;
import com.intellij.openapi.vfs.VirtualFile;
import com.intellij.util.messages.Topic;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.training.BadCourseException;
import org.jetbrains.training.BadLessonException;
import org.jetbrains.training.LessonIsOpenedException;

import java.awt.*;
import java.io.IOException;
import java.util.EventListener;
import java.util.concurrent.ExecutionException;

/**
 * Created by karashevich on 27/02/15.
 */
public interface LessonListener extends EventListener {

    void lessonStarted(Lesson lesson);

    void lessonPassed(Lesson lesson);

    void lessonClosed(Lesson lesson);

    void lessonNext(Lesson lesson) throws BadLessonException, ExecutionException, IOException, FontFormatException, InterruptedException, BadCourseException, LessonIsOpenedException;

}