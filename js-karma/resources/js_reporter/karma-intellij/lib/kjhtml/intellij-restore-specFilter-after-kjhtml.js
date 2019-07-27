/*
 * Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

(function (window) {

  var queryString = new jasmine.QueryString({
    getWindowLocation: function() { return window.location; }
  });
  if (queryString.getParam('spec') == null) {
    var env = jasmine.getEnv();
    if (env.intellijPrevSpecFilter != null) {
      if (typeof env.configuration === 'function') {
        var configuration = env.configuration() || {};
        configuration.specFilter = env.intellijPrevSpecFilter;
        env.configure(configuration);
      }
      else {
        env.specFilter = env.intellijPrevSpecFilter;
      }
    }
  }

})(window);
