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
