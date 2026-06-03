'use strict';

const { add, subtract } = require('../src/math');

describe('math', () => {
  test('add', () => {
    expect(add(2, 3)).toBe(5);
  });

  test('subtract', () => {
    expect(subtract(4, 3)).toBe(1);
  });

  // multiply() and divide() are intentionally NOT tested, so coverage is partial.
});
