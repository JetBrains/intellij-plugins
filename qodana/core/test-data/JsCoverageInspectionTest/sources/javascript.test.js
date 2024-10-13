var foo = require('./javascript').foo;

describe('foo', () => {
    test('foo returns 42', () => {
        const result = foo();
        expect(result).toBe(42);
    });
});