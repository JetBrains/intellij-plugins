// Total complexity: 4
public class Example {
  static class MyException extends RuntimeException {
  }

  static class MyOtherException extends RuntimeException {
  }

  // Complexity[main]: 1 + 1 (if) + 2 (catch) = 4
  public static void main(String[] args) {
    try {
      if (args.length == 0) {
        throw new MyException();
      }
    }
    catch (MyException e) {
      System.out.println(e);
    }
    catch (MyOtherException e) {
      System.out.println(e);
    }
    finally {
      System.out.println("Finally");
    }
  }
}