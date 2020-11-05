
class QuadraticEquationsSolver
  def discriminant(a, b, c)
    b * b - 4 * a * c
  end

  # We assume all coefficients are nonzero
  # and we will find only real roots
  def solve(a, b, c)
    d = discriminant(a, b, c)
    if d < 0
      puts "No roots"
    elsif d > 0
      x1 = (-b + sqrt(d)) / (2.0 * a)
      x2 = (-b - sqrt(d)) / (2.0 * a)
      print "x1 = %0.3f, x2 = %0.3f" % [x1, x2]
    else
      print "x = %0.3f" % [(-b) / (2.0 * a)]
    end
  end
end