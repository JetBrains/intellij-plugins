package sources

import "testing"

func TestAdd(t *testing.T) {
	if got, want := Foo(1, 2), 3; got != want {
		t.Errorf("add method produced wrong result. expected: %d, got: %d", want, got)
	}
}

func TestBaz(t *testing.T) {
	if got, want := Baz(), 22; got() != want {
		t.Errorf("add method produced wrong result. expected: %d, got: %d", want, got())
	}
}

func TestGaz(t *testing.T) {
	if got, _ := Gaz(1), 24; got == nil {
		t.Errorf("wrong result")
	}
}

func TestA(t *testing.T) {
	if got, want := A(), 42; got != want {
		t.Errorf("add method produced wrong result. expected: %d, got: %d", want, got)
	}
}

func TestB(t *testing.T) {
	if got, want := B(), 12; got != want {
		t.Errorf("add method produced wrong result. expected: %d, got: %d", want, got)
	}
}
