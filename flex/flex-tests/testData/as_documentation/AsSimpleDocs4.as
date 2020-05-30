package 
{

  public class AAA {
      /**
       * Useful information
       */
    function get aaa():CCC { return 1; }
    
    function set aaa(x:CCC) {}

    function foo() {
        this.a<caret>aa = 1;
    }
  }
}
