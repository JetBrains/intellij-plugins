export class Pet {
    constructor(name, ownerName) {
        this.name = name;
        this.ownerName = ownerName;
    }

    get owner() {
        return this.ownerName;
    }
}