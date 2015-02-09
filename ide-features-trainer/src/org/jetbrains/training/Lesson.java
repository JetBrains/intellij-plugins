package org.jetbrains.training;

import org.jdom.JDOMException;
import org.jetbrains.annotations.Nullable;

import java.io.IOException;

/**
 * Created by karashevich on 29/01/15.
 */
public class Lesson {

    private Scenario scn;
    private String name;
    private boolean isPassed;
    private String targetPath;

    public Lesson(String pathToScenario, boolean passed) throws BadLessonException {
        try {
            scn = new Scenario(pathToScenario);
            name = scn.getName();
            isPassed = passed;
            targetPath = scn.getTarget();
        } catch (JDOMException e) {
            //Scenario file is corrupted
            throw new BadLessonException("Probably scenario file is corrupted");
        } catch (IOException e) {
            //Scenario file cannot be read
            throw new BadLessonException("Probably scenario file cannot be read");
        }
    }

    public void pass(){
        isPassed = true;
        scn.getRoot().getAttribute("passed").setValue("true");
        try {
            scn.saveState();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    public String getName() {
        return name;
    }

    public boolean isPassed(){
        return isPassed;
    }

    public Scenario getScn(){
        return scn;
    }

    public String getTargetPath() {
        return targetPath;
    }
}
