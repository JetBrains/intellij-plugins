/*
 * Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

(function () {
  var karma = window.__karma__;
  if (typeof karma !== 'undefined' && typeof Proxy !== 'undefined') {
    window.__karma__ = new Proxy(karma, {
      set: function(target, property, value) {
        var newValue = value;
        if (property === 'start' && typeof value === 'function') {
          newValue = function() {
            var args = Array.prototype.slice.call(arguments);
            setTimeout(function() {
              value.apply(target, args);
            }, 500);
          };
        }
        target[property] = newValue;
        return true;
      }
    });
  }
})();
