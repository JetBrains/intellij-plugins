import * as i0 from "@angular/core";
import {Component} from "@angular/core";

export declare class TuiLetContext<T> {
  private readonly <warning descr="Unused readonly field internalDirectiveInstance">internalDirectiveInstance</warning>;
  constructor(internalDirectiveInstance: TuiLet<T>);
  get <warning descr="Unused property $implicit">$implicit</warning>(): T;
  get <warning descr="Unused property tuiLet">tuiLet</warning>(): T;
}

export declare class TuiLet<T> {
  tuiLet: T;
  constructor();
  /**
   * Asserts the correct type of the context for the template that `TuiLet` will render.
   *
   * The presence of this method is a signal to the Ivy template type-check compiler that the
   * `TuiLet` structural directive renders its template with a specific context type.
   */
  static ngTemplateContextGuard<T>(_dir: TuiLet<T>, _ctx: unknown): _ctx is TuiLetContext<T>;
  static ɵfac: i0.ɵɵFactoryDeclaration<TuiLet<any>, never>;
  static ɵdir: i0.ɵɵDirectiveDeclaration<TuiLet<any>, "[tuiLet]", never, { "tuiLet": { "alias": "tuiLet"; "required": false; }; }, {}, never, never, true, never>;
}

@Component({
   selector: 'app-test',
   standalone: true,
   imports: [
     TuiLet
   ],
   template: `
   <ng-container *tuiLet="items as filteredItems">
        {{check(<error descr="TS2345: Argument of type 'string[]' is not assignable to parameter of type 'number'.">filteredItems</error>)}}
   </ng-container>
   `,
 })
export class TestComponent {

  items!: string[]

  check(_foo:number) {

  }

}
