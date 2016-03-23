package training.statistic;

import com.intellij.internal.statistic.CollectUsagesException;
import com.intellij.internal.statistic.UsagesCollector;
import com.intellij.internal.statistic.beans.GroupDescriptor;
import com.intellij.internal.statistic.beans.UsageDescriptor;
import org.jetbrains.annotations.NotNull;
import training.learn.Module;
import training.learn.CourseManager;
import training.learn.Lesson;
import training.learn.MyPair;

import java.util.HashSet;
import java.util.Set;

/**
 * Created by Sergey Karashevich on 02/02/16.
 */
public class LessonUsageCollector  extends UsagesCollector{
    private static final int PROTOCOL_VERSION = 1;

    @NotNull
    @Override
    public Set<UsageDescriptor> getUsages() throws CollectUsagesException {
        Set<UsageDescriptor> result = new HashSet<>();

        Module[] modules = CourseManager.getInstance().getModules();
        for (Module module : modules) {
            for (Lesson lesson : module.getLessons()) {
                for (MyPair myPair : lesson.getStatistic()) {
                    result.add(new UsageDescriptor("module>" + module.getName() +
                            ">lesson>" + lesson.getName() +
                            ">status>" + myPair.getStatus() +
                            ">time>" + myPair.getTimestamp(), 1));
                }
            }
        }
        return result;
    }

    @NotNull
    @Override
    public GroupDescriptor getGroupId() {
        return GroupDescriptor.create("plugin.training.v" + PROTOCOL_VERSION);
    }

}
