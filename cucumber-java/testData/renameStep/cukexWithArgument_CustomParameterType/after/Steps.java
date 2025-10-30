import io.cucumber.java.ParameterType;
import io.cucumber.java.en.Then;

public class Steps {

  @Then("some other step")
  public void foo() {
  }

  @Then("a type {customType} is mentioned")
  public void customParameterTypeIsUsed(CustomType x) {
  }

  @ParameterType(".*")
  public CustomType customType(String arg) {
    return new CustomType(arg);
  }

  public static class CustomType {
    public CustomType(String arg) {
    }
  }
}
