/*
QUnitAdapter

Run qunit tests using Google's JS Test Driver. Maps async methods stop() and start().

This provides almost the same api as qunit. Extended from original adapter by Karl Okeeffe. 
*/
(function() {
	if(!(window.equiv)) {
		throw new Error("QUnitAdapter.js - Unable to find equiv function. Ensure you have added equiv.js to the load section of your jsTestDriver.conf");
	}
	
	var QUnitTestCase, lifecycle;

	window.module = function(name, lifecycle_) {
    	QUnitTestCase = AsyncTestCase(name);
    	lifecycle = lifecycle_ || {};
	};
    
    window.test = function(name, expected, test) {
    	QUnitTestCase.prototype['test ' + name] = (function(lifecycle) {
    		return function(q) {
    			var context = {},
    				origStop = window.stop;

    			// setup
    			if (lifecycle.setup) {
    				lifecycle.setup.call(context);
    			}
    			
    			// expected is an optional argument
    			if(expected.constructor === Number) {
    				expectAsserts(expected);
    			} else {
    				test = expected;
    			}

			    window.stop = function() {
			    	var capturedAssertions = [],
			    		originalAssertions = [],
			    		assertions = ['ok', 'equal', 'notEqual', 'deepEqual', 'notDeepEqual', 
			    		              'strictEqual', 'notStrictEqual', 'raises'];
			    	
			    	for (var i = 0; i < assertions.length; i++) {
			    		originalAssertions[assertions[i]] = window[assertions[i]];	
			    	}
			    	
			    	window.ok = function() { capturedAssertions.push(['ok', arguments]) };
					window.equal = function() { capturedAssertions.push(['equal', arguments]) };
			    	window.notEqual = function() { capturedAssertions.push(['notEqual', arguments]) };
			    	window.deepEqual = function() { capturedAssertions.push(['deepEqual', arguments]) };
			    	window.notDeepEqual = function() { capturedAssertions.push(['notDeepEqual', arguments]) };
			    	window.strictEqual = function() { capturedAssertions.push(['strictEqual', arguments]) };
			    	window.notStrictEqual = function() { capturedAssertions.push(['notStrictEqual', arguments]) };
			    	window.raises = function() { capturedAssertions.push(['raises', arguments]) };
					
			    	// This could be a more efficient way of doing the above, but can't achieve correct scope
			    	// capturedAssertions.push() is called upon function call rather than upon setup,
			    	// so actual values never enter the array, only 'undefined'
			    	/*
			    	for (var i = 0; i < assertions.length; i += 1) {
			    		window[assertions[i]] = function() {
			    			capturedAssertions.push([assertions[i], arguments]);
						};
			    	}
			    	*/
			    	
					// Sets up test to resume when `start()` is called.
					q.defer('start()', function(pool) {
					    var origStart = window.start;
					    
					    window.start = pool.add(function() {
					        window.start = origStart;
					    });
					});
					
					// Assertions made in async tests must run in a `defer()` callback.
					q.defer('async assertions', function(pool) {
					    var assertion;

					    for (var i = 0; i < assertions.length; i++) {
					    	 window[assertions[i]] = originalAssertions[assertions[i]];	
				    	}
					    
					    for (var i = 0; i < capturedAssertions.length; i++) {
					        assertion = capturedAssertions[i];
					        window[assertion[0]].apply(null, assertion[1]);
					    }
					});
			    };

			    test.call(context);

			    window.stop = origStop;

			    // teardown
			    if (lifecycle.teardown) {
			    	lifecycle.teardown.call(context);
			    }
    		};
    	})(lifecycle);  // capture current value of `lifecycle` in new scope
    };

    // wrapper to provide async functionality
    window.asyncTest = function(name, expected, test) {
    	var testFn = function() {
    		window.stop();
    		// expected is an optional argument
    		test = !test ? expected : test;
    		test.call(this);
    	};
    	
    	if (!test) {
    		window.test(name, testFn);
    	} else {
    		window.test(name, expected, testFn);
    	}
    };
    
    window.expect = function(count) {
        expectAsserts(count);
    };

    window.ok = function(actual, msg) {
        assertTrue(msg ? msg : '', !!actual);
    };
    
    window.equal = function(a, b, msg) {
        assertEquals(msg ? msg : '', b, a);
    };
    
    window.notEqual = function(a, b, msg) {
    	assertNotEquals(msg ? msg : '', b, a);
    };

    window.deepEqual = function(a, b, msg) {
    	assertTrue(msg ? msg : '', window.equiv(b, a));
    };
    
    window.notDeepEqual = function(a, b, msg) {
    	assertTrue(msg ? msg : '', !window.equiv(b, a));
    };
    
    window.strictEqual = function(a, b, msg) {
    	assertSame(msg ? msg : '', b, a);
    };
    
    window.notStrictEqual = function(a, b, msg) {
    	assertNotSame(msg ? msg : '', b, a);
    };
    
    // error argument must be a function
    window.raises = function(callback, error, msg) {
    	if(!msg) {
    		assertException(error, callback);
		} else {
    		assertException(msg, callback, error);
    	}
    }
    
    // support for depreciated QUnit methods
    window.equals = window.equal;
    window.same = window.deepEqual;
    
    window.reset = function() {
    	fail('reset method is not available when using JS Test Driver');
    };

    window.isLocal = function() {
    	return false;
    };
    
    window.QUnit = {
    	equiv: window.equiv,
    	ok: window.ok
    };

    // we need at least a single module to prevent jsTestDriver erroring out with exception
	module('Default Module');

})();