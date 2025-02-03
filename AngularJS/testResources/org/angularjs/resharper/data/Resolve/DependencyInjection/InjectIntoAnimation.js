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
