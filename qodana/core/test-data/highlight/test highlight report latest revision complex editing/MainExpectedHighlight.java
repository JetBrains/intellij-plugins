public class Main {

  public static void <qodana_problem descr="[High] Qodana: main">main</qodana_problem>(String[] args) {
  }
  void functionWithSpacesIndent() {
    //new comment
    int i = 0;
    if (true) {
      //<qodana_problem descr="[High] Qodana: code in functionWithSpacesIndent">code</qodana_problem> hi
    }
    int j = i;
  }

  void empty() {};

	void functionWithTabulationIndent() {
		//<qodana_problem descr="[High] Qodana: code in functionWithTabulationIndent">code</qodana_problem> hi more text
		if (true){}
		if (true) {
			System.out.println(1);
		}
	}
}