package coversingle

// Add returns the sum of two integers.
func Add(a, b int) int {
	return a + b
}

// Sub returns the difference of two integers.
func Sub(a, b int) int {
	return a - b
}

// Sign returns -1, 0 or 1 depending on the sign of n.
// Only part of this function is exercised by the tests.
func Sign(n int) int {
	if n > 0 {
		return 1
	}
	if n < 0 {
		return -1
	}
	return 0
}
