// Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
module.value('myOpacity', 0.5);

module.animation('.enter-in', function(myOpacity) {
    return {
        enter: function(element, done) {
            jQuery(element).animate({
                opacity: myOpacity
            }, done);
        }
    };
});
