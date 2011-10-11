/*suite id:1, name:some suite*/describe('some suite', function () {

    var suiteWideFoo;

    beforeEach(function () {
        suiteWideFoo = 0;
    });

    /*suite id:1_1, name:some nested suite*/describe('some nested suite', function() {
        var nestedSuiteBar;
        beforeEach(function() {
            nestedSuiteBar = 1;
        });

        /*spec id:1_1_1, name:nested expectation*/it('nested expectation', function () {
            expect(suiteWideFoo).toEqual(0);
            expect(nestedSuiteBar).toEqual(1);
        })/*specEnd id:1_1_1*/;

    })/*suiteEnd id:1_1*/;

    /*spec id:1_2, name:top-level describe*/it('top-level describe', function () {
        expect(suiteWideFoo).toEqual(0);
        expect(nestedSuiteBar).toEqual(undefined); // Spec will fail with ReferenceError: nestedSuiteBar is not defined
    })/*specEnd id:1_2*/;
})/*suiteEnd id:1*/;
