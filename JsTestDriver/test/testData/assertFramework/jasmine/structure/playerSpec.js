// Song class declaration

function Song() {}

Song.prototype.persistFavoriteStatus = function(value) {
  // something complicated
  throw new Error("not yet implemented");
};

// Player class declaration

function Player() {}

(function(playerPrototype) {
    playerPrototype.play = function(song) {
        this.currentlyPlayingSong = song;
        this.isPlaying = true;
    };
    playerPrototype.pause = function() {
        this.isPlaying = false;
    };
    playerPrototype.resume = function() {
        if (this.isPlaying) {
            throw new Error("song is already playing");
        }
        this.isPlaying = true;
    };
    playerPrototype.makeFavorite = function() {
        this.currentlyPlayingSong.persistFavoriteStatus(true);
    };
}(Player.prototype));

// beforeEach configuration
beforeEach(function() {
  this.addMatchers({
    toBePlaying: function(expectedSong) {
      var player = this.actual;
      return player.currentlyPlayingSong === expectedSong
          && player.isPlaying;
    }
  })
});


/*suite id:1, name:Player*/describe("Player", function() {
    var player;
    var song;

    beforeEach(function() {
        player = new Player();
        song = new Song();
    });

    /*spec id:1_1, name:should be able to play a Song*/it("should be able to play a Song", function() {
        player.play(song);
        expect(player.currentlyPlayingSong).toEqual(song);

        //demonstrates use of custom matcher
        expect(player).toBePlaying(song);
    })/*specEnd id:1_1*/;

    /*suite id:1_2, name:when song has been paused*/describe("when song has been paused", function() {
        beforeEach(function() {
            player.play(song);
            player.pause();
        });

        /*spec id:1_2_1, name:should indicate that the song is currently paused*/it("should indicate that the song is currently paused", function() {
            expect(player.isPlaying).toBeFalsy();

            // demonstrates use of 'not' with a custom matcher
            expect(player).not.toBePlaying(song);
        })/*specEnd id:1_2_1*/;

        /*spec id:1_2_2, name:should be possible to resume*/it("should be possible to resume", function() {
            player.resume();
            expect(player.isPlaying).toBeTruthy();
            expect(player.currentlyPlayingSong).toEqual(song);
        })/*specEnd id:1_2_2*/;
    })/*suiteEnd id:1_2*/;

    // demonstrates use of spies to intercept and test method calls
    /*spec id:1_3, name:tells the current song if the user has made it a favorite*/it("tells the current song if the user has made it a favorite", function() {
        spyOn(song, 'persistFavoriteStatus');

        player.play(song);
        player.makeFavorite();

        expect(song.persistFavoriteStatus).toHaveBeenCalledWith(true);
    })/*specEnd id:1_3*/;

    //demonstrates use of expected exceptions
    /*suite id:1_4, name:#resume*/describe("#resume", function() {
        /*spec id:1_4_1, name:should throw an exception if song is already playing*/it("should throw an exception if song is already playing", function() {
            player.play(song);

            expect(
                function() {
                    player.resume();
                }).toThrow("song is already playing");
        })/*specEnd id:1_4_1*/;
    })/*suiteEnd id:1_4*/;
})/*suiteEnd id:1*/;
