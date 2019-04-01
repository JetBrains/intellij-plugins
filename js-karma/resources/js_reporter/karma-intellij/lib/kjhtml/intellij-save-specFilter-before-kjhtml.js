/*
 * Copyright 2000-2018 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
 */

(function(window){

  var env = jasmine.getEnv();
  if (typeof env.configuration === 'function') {
    env.intellijPrevSpecFilter = env.configuration().specFilter;
  }
  else {
    env.intellijPrevSpecFilter = env.specFilter;
  }

})(window);
