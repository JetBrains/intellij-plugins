interface Person {
    firstName: string;
    lastName: string;
    age: number;
}

class Employee implements Person {
    constructor(
                public firstName: string,
        public lastName: string,
                public age: number,
        private department: string,
                private salary: number) {
    }

    getFullName(): string {
        return this.firstName + " " + this.lastName;
    }

    getDetails(): {name: string, age: number, department: string} {
        return {name: this.getFullName(), age: this.age, department: this.department};
    }
}