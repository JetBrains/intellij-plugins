// use reexport for correct parsing (current parser can't parse with `QUnit.` part)
const module = QUnit.module;
const test = QUnit.test;

module('user')

test('should be tested', (assert) => {
  assert.ok(true);
});

test('should be tested 2', (assert) => {
  assert.ok(true);
});
