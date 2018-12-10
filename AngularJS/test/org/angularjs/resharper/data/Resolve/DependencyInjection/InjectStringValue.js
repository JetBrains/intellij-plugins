// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
module.value('myValue', 'hello world');

module.controller('myController', function(myValue) {
    return myValue;
});
