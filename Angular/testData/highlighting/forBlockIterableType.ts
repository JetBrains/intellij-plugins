import {Component} from "@angular/core";
import {Observable, of} from 'rxjs';
import {AsyncPipe} from "@angular/common";

interface Person {
  id:number;
  name: string;
  phone: string
}

@Component({
   selector: 'app-test',
   standalone: true,
   template: `
      @for (p of <error descr="Type Person must have a [Symbol.iterator]() method that returns an iterator.">person</error>; track p.id) {
          {{ p.phone + p.foo }}
      }
      @for (p of persons; track p.id) {
          {{ p.phone + p.<error descr="TS2339: Property 'foo' does not exist on type 'Person'.">foo</error> }}
      }
      @for (p of personsAny; track p.id) {
          {{ p.phone + p.foo }}
      }
      @for (p of personsOptional; track p.id) {
          {{ p.phone + p.<error descr="TS2339: Property 'foo' does not exist on type 'Person'.">foo</error> }}
      }
      @for (p of (persons$ | async); track p.id) {
          {{ p.phone + p.<error descr="TS2339: Property 'foo' does not exist on type 'Person'.">foo</error> }}
      }
      @for (t of title; track t) {
        {{t.<error descr="TS2339: Property 'foo' does not exist on type '\"a\" | \"b\" | \"c\" | \"d\"'.
  Property 'foo' does not exist on type '\"a\"'.">foo</error>}}
      }
    `,
   imports: [AsyncPipe]
 })
export class TestComponent {
  person!: Person
  persons!: Person[]
  personsAny: any
  personsOptional?: Person[]
  persons$: Observable<Person[]> = of(this.persons);
  title= ['a', 'b', 'c', 'd'] as const;
}
