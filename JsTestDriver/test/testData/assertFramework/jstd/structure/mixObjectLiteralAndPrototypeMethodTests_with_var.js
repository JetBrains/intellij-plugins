var testCase = /*TestCase id:1, name:my test case*/TestCase("my test case", {
    /*Test id:1_1, name:testArray, type:property*/testArray: function() {
        var a = [1, 2, 3];
        assertEquals("", true, a.contains(1));
    }/*TestEnd id:1_1, type:property*/
})/*TestCaseEnd id:1*/;

testCase.prototype./*Test id:1_2, name:testMy, type:declaration*/testMy/*TestEnd id:1_2, type:declaration*/ = /*Test id:1_2, type:body*/function() {

}/*TestEnd id:1_2, type:body*/;
