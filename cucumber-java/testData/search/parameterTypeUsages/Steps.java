package calendar;

import io.cucumber.core.api.TypeRegistry;
import io.cucumber.core.api.TypeRegistryConfigurer;
import io.cucumber.cucumberexpressions.ParameterType;
import io.cucumber.java.en.And;
import io.cucumber.java8.En;

import java.text.SimpleDateFormat;
import java.util.Date;

public class Steps implements En {

  public Steps() {
    Given("the day before yesterday is {isoDate}", (Object date) -> {
    });
  }

  @And("today is {isoDate}")
  public void today(Date today) {
  }

  @And("yesterday was {isoDate}, before was {isoDate}")
  public void yesterday_and_today(Date yesterday, Date today) {
  }

  public void configureTypeRegistry(TypeRegistry typeRegistry) {
    ParameterType<Date> parameterType = new ParameterType<>(
      "iso<caret>Date",
      "\\d{4}-\\d{2}-\\d{2}",
      Date.class,
      (String s) -> new SimpleDateFormat("yyyy-mm-dd").parse(s)
    );
  }
}
