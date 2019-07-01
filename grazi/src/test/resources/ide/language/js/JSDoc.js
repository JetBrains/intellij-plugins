/**
 * module description <warning>eror</warning>
 * @module MyClass
 */
//constants to be documented.
/** @const {String} some constant <warning>discription</warning>*/
const CONST_1 = "1";
/** @const {Number} <warning>a</warning> another example  */
const CONST_2 = 2;

//An example class
/** MyClass description */
class MyClass {

    //the class constructor
    /**
     * constructor description
     * @param  {[type]} config class description with <warning>an errors</warning>
     */
    constructor(config) {
        //class members. Should be private.
        /** @private */
        this.member1 = config;
        /** @private */
        this.member2 = "bananas";
    }

    //A normal method, public
    /** methodOne description */
    methodOne() {
        console.log("methodOne");
    }

    //Another method. Receives a Fruit object parameter, public
    /**
     * methodTwo description
     * @param  {Object} fruit      param <warning>descriproin</warning>
     * @param  {String} fruit.nme  nme of the fruit
     * @return {String}            return values description
     */
    methodTwo(fruit) {
        return "he likes " + fruit.nme;
    }
}

module.exports = MyClass;