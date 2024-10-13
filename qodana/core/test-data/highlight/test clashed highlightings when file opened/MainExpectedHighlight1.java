public class Main {

  public static void main(String[] args) {
    // write your code here
  }

  void functionWithSpacesIndent() {
    //code hi
  }

	void functionWithTabulationIndent() {
		//code hi
		<qodana_problem descr="[High] Qodana: Empty body">if</qodana_problem> (true){}
    if (true) {
			System.out.println(1);
		}
	}
}