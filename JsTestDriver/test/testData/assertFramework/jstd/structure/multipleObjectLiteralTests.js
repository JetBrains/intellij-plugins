/*TestCase name:multiply test-case, id:1*/TestCase("multiply test-case", {
    /*Test id:1_1, name:test1, type:property*/test1: function() {

    }/*TestEnd id:1_1, type:property*/,
    /*Test id:1_2, name:test string literal test name, type:property*/"test string literal test name" : function() {
        expectAsserts(1);
        assertTrue(true);
    }/*TestEnd id:1_2, type:property*/,
    "wrong test name" : function() {
        expectAsserts(1);
        assertTrue(true);
    }
})/*TestCaseEnd id:1*/;
