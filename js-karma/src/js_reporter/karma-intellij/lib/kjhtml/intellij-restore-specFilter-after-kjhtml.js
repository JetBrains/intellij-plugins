(function (window) {

  var queryString = new jasmine.QueryString({
    getWindowLocation: function() { return window.location; }
  });
  if (queryString.getParam('spec') == null) {
    var env = jasmine.getEnv();
    if (env.intellijPrevSpecFilter != null) {
      env.specFilter = env.intellijPrevSpecFilter;
    }
  }

})(window);
