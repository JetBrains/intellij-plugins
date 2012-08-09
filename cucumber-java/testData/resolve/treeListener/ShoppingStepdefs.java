public class ShoppingStepdefs {
  @Then("^my change should be (\\d+)$")
  public void my_change_should_be_(int change) {
    assertEquals(-calc.value().intValue(), change);
  }
}
