package coversingle

import "testing"

// Tests intentionally cover only PART of the package:
// Add is tested, Sign is partially tested (positive branch only),
// and Sub is left untested.

func TestAdd(t *testing.T) {
	if got := Add(2, 3); got != 5 {
		t.Fatalf("Add(2,3) = %d, want 5", got)
	}
}

func TestSignPositive(t *testing.T) {
	if got := Sign(7); got != 1 {
		t.Fatalf("Sign(7) = %d, want 1", got)
	}
}
