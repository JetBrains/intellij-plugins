import {Pet} from './pet'

export class Dog extends Pet {
    constructor(name, ownerName, breed) {
        super(name, ownerName);
        this.breed = breed;
    }

    giveTreat(favoriteTreat) {
        console.log(`${this.ownerName} gives ${this.name} ${favoriteTreat}`)
    }
}

let snoopy = new Dog('Snoopy', 'Charlie', 'Beagle');

snoopy.giveTreat('pizza');