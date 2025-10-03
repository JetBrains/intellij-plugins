package example;

import io.cucumber.java.en.Given;
import io.cucumber.java.ParameterType;

public class Steps {
  public Steps() {
    toMood("some another usage");
  }

  @Given("today is {newMoodName}")
  public void step_method(Mood mood) {
  }

  @ParameterType(value = ".*", name = "newMoodName")
  public Mood toMood(String moodName) {
    return Mood.valueOf(moodName);
  }

  public enum Mood {HAPPY, SAD}
}
