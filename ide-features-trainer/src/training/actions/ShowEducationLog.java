package training.actions;

import com.intellij.ide.actions.ShowFilePathAction;
import com.intellij.openapi.actionSystem.AnAction;
import com.intellij.openapi.actionSystem.AnActionEvent;
import com.intellij.openapi.actionSystem.Presentation;
import com.intellij.openapi.application.PathManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.util.SmartList;
import org.jetbrains.annotations.NotNull;
import training.lesson.*;
import training.lesson.log.GlobalLessonLog;
import training.lesson.log.LessonLog;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;

/**
 * Created by karashevich on 07/10/15.
 */
public class ShowEducationLog extends AnAction implements DumbAware {

    private final static String EDULOG = "eduLog.log";

    public void actionPerformed(AnActionEvent e) {
        final File eduLogFile = createEduLogFile();
        try {
            PrintWriter writer = new PrintWriter(eduLogFile, "UTF-8");
            copyEduLogToWriter(writer);
            writer.close();
            ShowFilePathAction.openFile(eduLogFile);

        } catch (FileNotFoundException e1) {
            e1.printStackTrace();
        } catch (UnsupportedEncodingException e1) {
            e1.printStackTrace();
        }

    }

    //create or overwrite file where Education Log info will be copied
    private File createEduLogFile() {
        return new File(PathManager.getLogPath(), EDULOG);
    }

    private void copyEduLogToWriter(PrintWriter writer) {
        assert writer != null;

        final GlobalLessonLog globalLessonLog = CourseManager.getInstance().getGlobalLessonLog();
        final Course[] courses = CourseManager.getInstance().getCourses();
        for (Course course : courses) {
            writer.println("COURSE:" + course.getName().toUpperCase());
            final ArrayList<Lesson> lessons = course.getLessons();
            for (Lesson lesson : lessons) {
                writer.print("LESSON:" + lesson.getName());
                if(lesson.getPassed()) {
                    writer.println(" (passed)");
                } else {
                    writer.println();
                }
                if (globalLessonLog.globalLessonLogMap.containsKey(lesson.getName())) {
                    final SmartList<LessonLog> lessonLogs = globalLessonLog.globalLessonLogMap.get(lesson.getName());
                    int i = 0;
                    for (LessonLog lessonLog : lessonLogs) {
                        writer.println("lesson log(" + i++ + "):");
                        writer.println(lessonLog.exportToString());
                    }
                }
                writer.println();
            }
            writer.println("#####################\n");
        }
    }

    @Override
    public void update(AnActionEvent e) {
        Presentation presentation = e.getPresentation();
        presentation.setVisible(ShowFilePathAction.isSupported());
        presentation.setText(getActionName());
    }

    @NotNull
    public static String getActionName() {
        return (EducationBundle.message("action.showEduLog.description") + " " + ShowFilePathAction.getFileManagerName());
    }

    //INTERNAL: print descriptions for tech writers
    private static void printDescriptions() {
        final Course[] courses = CourseManager.getInstance().getCourses();
        for (Course course : courses) {
            System.out.println("\n\n##############################");
            System.out.println(course.getName().toUpperCase());
            System.out.println("##############################");
            final ArrayList<Lesson> lessons = course.getLessons();
            for (Lesson lesson : lessons) {
                System.out.println("\n" + lesson.getName());
                System.out.println(LessonProcessor.takeDescriptionsOnly(lesson));
            }
        }
    }

}
