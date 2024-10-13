import java.util.ArrayList;

/**
 * Hello world!
 *
 */
public class App
{
    public static void main( String[] args )
    {
        System.out.println( "Hello World!" );
    }

    public void duplicatedMethod1() {
        ArrayList<String> strings = new ArrayList<>();
        strings.add("mmm");
        strings.add("mmm");
        strings.add("mmm");
        strings.add("mmm");
        for (String string : strings) {
            string.hashCode();
        }
    }

    public void duplicatedMethod2() {
        ArrayList<String> strings = new ArrayList<>();
        strings.add("mmm");
        strings.add("mmm");
        strings.add("mmm");
        strings.add("mmm");
        for (String string : strings) {
            string.hashCode();
        }
    }
}