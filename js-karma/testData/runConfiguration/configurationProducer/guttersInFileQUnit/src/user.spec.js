// Copyright 2000-2025 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.

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
