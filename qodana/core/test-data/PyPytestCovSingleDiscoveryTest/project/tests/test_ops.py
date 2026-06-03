"""Partial test suite: covers add, subtract and the happy path of divide.
multiply, factorial and is_prime are left uncovered on purpose."""

from mathlib.ops import add, subtract, divide


def test_add():
    assert add(2, 3) == 5


def test_subtract():
    assert subtract(5, 2) == 3


def test_divide_ok():
    assert divide(10, 2) == 5
