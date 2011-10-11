var is_valid = function(email){
  var reg_expr = /^([A-Za-z0-9_\-\.])+@([A-Za-z0-9_\-\.])+\.([A-Za-z]{2,4})$/;
  return reg_expr.test(email);
};

/*suite id:1, name:Email validation*/describe("Email validation", function() {

    /*spec id:1_2, name:should validate someone@somewhere.com*/it("should validate someone@somewhere.com", function() {
        var result = is_valid("someone@somewhere.com");
        expect(result).toBe(true);
    })/*specEnd id:1_2*/;

    /*spec id:1_3, name:should not validate someone@somewhere*/it("should not validate someone@somewhere", function() {
        var result = is_valid("someone@somewhere");
        expect(result).not.toBe(true);
    })/*specEnd id:1_3*/;

})/*suiteEnd id:1*/;
