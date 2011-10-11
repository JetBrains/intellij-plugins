/*test name:a basic test example, id:0_1*/test("a basic test example", function() {
  ok( true, "this test is fine" );
  var value = "hello";
  equal( value, "hello", "We expect value to be hello" );
})/*testEnd id:0_1*/;

/*module name:Module A, id:1*/module("Module A")/*moduleEnd id:1*/;

/*test name:first test within module, id:1_1*/test("first test within module", function() {
  ok( true, "all pass" );
})/*testEnd id:1_1*/;

/*test name:second test within module, id:1_2*/test("second test within module", function() {
  ok( true, "all pass" );
})/*testEnd id:1_2*/;

/*module name:Module B, id:2*/module("Module B")/*moduleEnd id:2*/;

/*test name:some other test, id:2_1*/test("some other test", function() {
  expect(2);
  equal( true, false, "failing test" );
  equal( true, true, "passing test" );
})/*testEnd id:2_1*/;
