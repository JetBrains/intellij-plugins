public class Main {

  public static void <qodana_problem descr="[High] Qodana: main">main</qodana_problem>(String[] args) {
    // write your code here
  }

  void functionWithSpacesIndent() {
    //<qodana_problem descr="[High] Qodana: code in functionWithSpacesIndent">code</qodana_problem> hi
  }

	void functionWithTabulationIndent() {
		//<qodana_problem descr="[High] Qodana: code in functionWithTabulationIndent">code</qodana_problem>
		if (true){}
		if (true) {
			System.out.println(1);
		}
	}
}