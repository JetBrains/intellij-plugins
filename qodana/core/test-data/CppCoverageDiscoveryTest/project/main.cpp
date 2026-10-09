int increment(int value) {
  return value + 1;
}

int decrement(int value) {
  return value - 1;
}

int main() {
  return increment(decrement(1));
}
