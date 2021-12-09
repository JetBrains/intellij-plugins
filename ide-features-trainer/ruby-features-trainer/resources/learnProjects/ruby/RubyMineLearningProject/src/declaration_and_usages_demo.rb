
puts "Enter 3 coefficients of full quadratic equation:"
values = STDIN.gets.split(" ").map(&:to_f)
a = values[0]
b = values[1]
c = values[2]
if a == 0 || b == 0 || c == 0
  puts "Any of coefficients is zero. It is not full quadratic equation."
else
  solver = QuadraticEquationsSolver.new
  d = solver.discriminant(a, b, c)
  puts "Discriminant of this equation is %0.3f" % [d]
  puts "Solution is:"
  solver.solve(a, b, c)
end