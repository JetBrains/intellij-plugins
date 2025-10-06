package example;

import cucumber.api.java.en.Given;
import cucumber.api.TypeRegistry;
import io.cucumber.cucumberexpressions.ParameterType;

import java.util.Date;
import java.text.SimpleDateFormat;

public class Steps {
  @Given("today is {iso<caret>-date}")
  public void step_method(Date arg1) throws Throwable {
  }

  public void configureTypeRegistry(TypeRegistry typeRegistry) {
    typeRegistry.defineParameterType(new ParameterType<>(
      "iso-date",
      "\\d{4}-\\d{2}-\\d{2}",
      Date.class,
      (String s) -> new SimpleDateFormat("yyyy-mm-dd").parse(s)
    ));
  }
}
