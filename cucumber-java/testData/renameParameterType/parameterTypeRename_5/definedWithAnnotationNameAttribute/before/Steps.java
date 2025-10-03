package example;

import io.cucumber.java.en.Given;
import io.cucumber.java.ParameterType;

public class Steps {
  public Steps() {
    toMood("some another usage");
  }

  @Given("today is {custom<caret>MoodName}")
  public void step_method(Mood mood) {
  }

  @ParameterType(value = ".*", name = "customMoodName")
  public Mood toMood(String moodName) {
    return Mood.valueOf(moodName);
  }

  public enum Mood {HAPPY, SAD}
}
