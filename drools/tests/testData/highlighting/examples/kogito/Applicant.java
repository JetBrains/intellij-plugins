
package examples.kogito;


public class Applicant {

  public static final String THIS_IS_A_CONSTANT = "HERE_I_AM";

  private String name;
  private int age;

  public Applicant() {
  }

  public Applicant(String name, int age) {
    this.name = name;
    this.age = age;
  }

  public String getName() {
    return name;
  }

  public void setName(String name) {
    this.name = name;
  }

  public int getAge() {
    return age;
  }

  public void setAge(int age) {
    this.age = age;
  }
}
