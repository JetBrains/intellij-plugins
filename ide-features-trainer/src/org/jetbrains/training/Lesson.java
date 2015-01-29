package org.jetbrains.training;

import org.jdom.JDOMException;

import java.io.IOException;

/**
 * Created by karashevich on 29/01/15.
 */
public class Lesson {

    private Scenario scn;
    private boolean isPassed;

    public Lesson(String pathToScenario) throws BadLessonException {
        try {
            scn = new Scenario(pathToScenario);
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
    }

    public boolean isPassed(){
        return isPassed;
    }

    public Scenario getScn(){
        return scn;
    }



}
