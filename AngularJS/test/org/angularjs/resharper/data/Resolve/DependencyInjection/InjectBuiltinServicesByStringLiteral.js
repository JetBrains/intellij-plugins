module.controller('myController', [ '$log', '$logProvider', function(l, lp) {
    lp.debugEnabled(true);
    l.debug('hello');
});
