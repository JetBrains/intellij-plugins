public class Main {

  public static void <qodana_problem descr="[High] Qodana: main">main</qodana_problem>(String[] args) {
    // write your code here
  }

  void functionWithSpacesIndent() {
    /* CHANGED_BEFORE */ <qodana_problem descr="[High] Qodana: code in functionWithSpacesIndent">code</qodana_problem> CHANGED_AFTER
  }

	void functionWithTabulationIndent() {
		//<qodana_problem descr="[High] Qodana: code in functionWithTabulationIndent">code</qodana_problem> hi
		if (true){}
		if (true) {
			System.out.println(1);
		}
	}
}
