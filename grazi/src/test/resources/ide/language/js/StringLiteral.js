

var oneTypo = "It is <warning>friend</warning> of human";
var oneSpellcheckTypo = "It is <warning>frend</warning> of human";
var fewTypos = "It <warning>are</warning> working for <warning>much</warning> warnings";
var ignoreTemplate = "It is ${fewTypos} friend";
var notIgnoreOtherMistakes = "It is <warning>friend</warning>. <warning>But</warning> I have a ${1} here";


var oneTypo = 'It is <warning>friend</warning> of human';
var oneSpellcheckTypo = 'It is <warning>frend</warning> of human';
var fewTypos = 'It <warning>are</warning> working for <warning>much</warning> warnings';
var ignoreTemplate = 'It is ${fewTypos} friend';
var notIgnoreOtherMistakes = 'It is <warning>friend</warning>. <warning>But</warning> I have a ${1} here';


console.log("It is <warning>friend</warning> of human");
console.log("It is <warning>frend</warning> of human");
console.log("It <warning>are</warning> working for <warning>much</warning> warnings");
console.log("It is ${fewTypos} friend");
console.log("It is <warning>friend</warning>. <warning>But</warning> I have a ${1} here");
