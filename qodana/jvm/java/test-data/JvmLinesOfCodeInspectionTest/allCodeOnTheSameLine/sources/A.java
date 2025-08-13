
import java.util.List; class A { private final List<Integer> numbers; A(List<Integer> numbers) { this.numbers = numbers; } public int sum() { int sum = 0; for (int num : numbers) { sum += num; } return sum; }}
