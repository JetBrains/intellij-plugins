import { FooCls, fun2 } from './FooCls';

describe('FooCls', () => {
    test('empty constructor', () => {
        const fooCls = new FooCls();
        expect(fooCls).toBeInstanceOf(FooCls);
    });

    test('foo method', () => {
        const fooCls = new FooCls();
        const result = fooCls.foo();
        expect(result).toBe(42);
    });

    test('bar method', () => {
        const fooCls = new FooCls();
        const evenInput = 2;
        const oddInput = 3;
        const evenResult = fooCls.bar(evenInput);
        const oddResult = fooCls.bar(oddInput);
        expect(evenResult).toBe(-1);
        expect(oddResult).toBe(42);
    });
});

describe('fun2', () => {
    test('fun2 returns 42', () => {
        const result = fun2();
        expect(result).toBe(42);
    });
});