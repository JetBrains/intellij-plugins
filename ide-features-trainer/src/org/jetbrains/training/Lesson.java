package org.jetbrains.training;

import org.jdom.JDOMException;

import java.io.IOException;

/**
 * Created by karashevich on 29/01/15.
 */
public class Lesson {

    private Scenario scn;
    private String name;
    private boolean isPassed;

    public Lesson(String pathToScenario, boolean passed) throws BadLessonException {
        try {
            scn = new Scenario(pathToScenario);
            name = scn.getName();
            isPassed = passed;
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
            System.err.println("State has been saved");
        } catch (IOException e) {
            System.err.println("Cannot save state");
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



}
