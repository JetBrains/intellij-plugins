public class Main {


  public static void main(String[] args) {
    <qodana_problem descr="[High] Qodana: comments">// write your code here</qodana_problem>
  }

  void functionWithSpacesIndent() {
    <qodana_problem descr="[High] Qodana: comments">//code hi</qodana_problem>
  }

	void functionWithTabulationIndent() {
		<qodana_problem descr="[High] Qodana: comments">//code hi</qodana_problem>
		if (true){}
		if (true) {
			System.out.println(1);
		}
	}
}