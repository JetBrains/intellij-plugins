// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
module.constant('myConstant', 42);

module.controller('myController', function(myConstant) {
    return myConstant * 2;
});
