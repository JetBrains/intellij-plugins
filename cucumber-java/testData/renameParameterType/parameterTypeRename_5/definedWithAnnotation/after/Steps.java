package example;

import io.cucumber.java.en.Given;
import io.cucumber.java.ParameterType;

public class Steps {
  public Steps() {
    // customMoodName("some another usage"); // Not implemented yet.
  }

  @Given("today is {newMoodName}")
  public void step_method(Mood mood) {
  }

  @ParameterType(".*")
  public Mood newMoodName(String moodName) {
    return Mood.valueOf(moodName);
  }

  public enum Mood {HAPPY, SAD}
}
