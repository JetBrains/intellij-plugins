package example;

import io.cucumber.java.en.Given;
import io.cucumber.java.ParameterType;

public class Steps {
  @Given("today is {custom<caret>MoodName}")
  public void step_method(Mood mood) {
  }

  @ParameterType(".*")
  public Mood mood(String moodName) {
    return Mood.valueOf(moodName);
  }

  public enum Mood {HAPPY, SAD}
}
