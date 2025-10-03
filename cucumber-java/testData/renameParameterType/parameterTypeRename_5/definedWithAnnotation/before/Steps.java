package example;

import io.cucumber.java.en.Given;
import io.cucumber.java.ParameterType;

public class Steps {
  public Steps() {
    // customMoodName("some another usage"); // Not implemented yet.
  }

  @Given("today is {custom<caret>MoodName}")
  public void step_method(Mood mood) {
  }

  @ParameterType(".*")
  public Mood customMoodName(String moodName) {
    return Mood.valueOf(moodName);
  }

  public enum Mood {HAPPY, SAD}
}
