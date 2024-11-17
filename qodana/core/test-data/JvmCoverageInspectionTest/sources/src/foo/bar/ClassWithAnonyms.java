package foo.bar;

import java.util.Arrays;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

public class ClassWithAnonyms {
    // Define the property as a Function with a lambda expression
    private Function<Integer, Integer> squareFunction = x -> x * x;

    private List<Integer> numbers = Arrays.asList(1, 2, 3, 4, 5, 6, 7, 8, 9, 10);

    public Runnable runnable = new Runnable() {
        @Override
        public void run() {
            // Filtering the even numbers
            List<Integer> evenNumbers = numbers.stream()
                    .filter(n -> n % 2 == 0)
                    .collect(Collectors.toList());
            System.out.println("Even numbers: " + evenNumbers);

            // Doubling the numbers
            List<Integer> doubledNumbers = numbers.stream()
                    .map(n -> n * 2)
                    .collect(Collectors.toList());
            System.out.println("Doubled numbers: " + doubledNumbers);

            // Sum of all numbers
            int sum = numbers.stream()
                    .reduce(0, Integer::sum);
            System.out.println("Sum of all numbers: " + sum);

            // Squaring the numbers using the anonymous function property
            List<Integer> squaredNumbers = numbers.stream()
                    .map(squareFunction)
                    .collect(Collectors.toList());
            System.out.println("Squared numbers: " + squaredNumbers);
        }
    };
}
