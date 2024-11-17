public class Main {
  //inserted line

  public static void <qodana_problem descr="[High] Qodana: main">main</qodana_problem>(String[] args) {
    // write your code here
  }
  //inserted line

  void functionWithSpacesIndent() {
    //<qodana_problem descr="[High] Qodana: code in functionWithSpacesIndent">code</qodana_problem> hi
  }
  //inserted line

	void functionWithTabulationIndent() {
		//<qodana_problem descr="[High] Qodana: code in functionWithTabulationIndent">code</qodana_problem> hi
		//inserted line
		if(true) {}
		if (true) {
			System.out.println(1);
		}
	}
}