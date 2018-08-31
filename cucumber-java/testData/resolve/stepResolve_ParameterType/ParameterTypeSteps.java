package cucumber.examples.java.calculator;

import cucumber.api.java.en.Given;
import cucumber.api.java.en.Then;
import cucumber.api.java.en.When;
import cucumber.api.java.en.And;

import java.util.List;

import cucumber.api.TypeRegistry;
import cucumber.api.TypeRegistryConfigurer;
import io.cucumber.cucumberexpressions.ParameterType;

import static java.text.DateFormat.MEDIUM;
import static java.text.DateFormat.getDateInstance;
import static java.util.Locale.ENGLISH;

import static org.junit.Assert.assertEquals;

public class ParameterTypeSteps {
  @And("today is {iso-date}")
  public void step_method(Date arg1) throws Throwable {
  }

  @And("{int} is int")
  public void step_method(int arg1) throws Throwable {
  }

  @And("{float} is float")
  public void step_method(float arg1) throws Throwable {
  }

  @And("{word} is word")
  public void step_method(String arg1) throws Throwable {
  }

  @And("{string} is string")
  public void step_string_method(String arg1) throws Throwable {
  }

  @Override
  public void configureTypeRegistry(TypeRegistry typeRegistry) {
    typeRegistry.defineParameterType(new ParameterType<>(
      "iso-date",
      "\\d{4}-\\d{2}-\\d{2}",
      Date.class,
      (String s) -> new SimpleDateFormat("yyyy-mm-dd").parse(s)
    ));
  }

}
