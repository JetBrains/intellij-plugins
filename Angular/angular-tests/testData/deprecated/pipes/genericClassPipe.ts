import { NgModule, Component, Pipe, PipeTransform } from "@angular/core";

export abstract class Animal {
  get eat() {
    return "*eating*";
  }
}

export class Dog extends Animal {
  get bark() {
    return "woof!";
  }
}


export abstract class AbstractAnimalPipe<T extends Animal> implements PipeTransform {
  transform(name: string): T {
    throw new Error("only a test");
  }
}

@Pipe({ name: "dog" })
export class DogPipe extends AbstractAnimalPipe<Dog> {
}

@Component({
  selector: "app-root",
  template: `
        <div>{{('Clifford' | dog).<caret> }}</div>
    `,
})
export class AppComponent {
}

@NgModule({
  declarations: [
    AppComponent,
    DogPipe
  ],
  bootstrap: [AppComponent]
})
export class AppModule {
}