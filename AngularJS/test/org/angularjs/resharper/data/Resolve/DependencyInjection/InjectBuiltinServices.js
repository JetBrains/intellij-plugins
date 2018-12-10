// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.

// Provider declared as a named function
// $get is simple function
module.controller('c1', function($cacheFactory, $cacheFactoryProvider) {
    var get = $cacheFactoryProvider.get;
    $cacheFactory.info();
});

// Provider declared as a named function
// $get method is injected function
module.controller('c2', function($anchorScroll, $anchorScrollProvider) {
    $anchorScrollProvider.disableAutoScrolling();
    $anchorScroll('#foo');
});

// Provider defined as an injected function, assigned to a variable
module.controller('c3', function($animate, $animateProvider) {
    $animateProvider.register('thing', function() {
        return {};
    });
    $animate.on('enter', container,
       function callback(element, phase) {
         // cool we detected an enter animation within the container
       }
    );
});

// Get method return value dependent on injected value
module.controller('c4', function($templateCache) {
    var x = $templateCache.remove('templateId.html');
});
