import cucumber.api.java8.En;

public class ShoppingStepdefs implements En {
  public ShoppingStepdefs() {
    Given("I have cukes in my belly", (Integer cukes) -> {
      System.out.format("Cukes: %n\n", cukes);
    });

    Given("one " + "two", () -> {
      System.out.format("Cukes: %n\n", cukes);
    });
  }
}
