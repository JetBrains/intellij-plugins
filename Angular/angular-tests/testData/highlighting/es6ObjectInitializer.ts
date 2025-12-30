import {Component, Input} from '@angular/core';

export type Product = {   id: string;   title: string;   description: string; };

@Component({
   selector: 'app-registration-form',
   standalone: true,
   template: './registration-form.component.html',
 })
export class RegistrationFormComponent {
  @Input()
  product!: Product
}

@Component({
   selector: 'app-root',
   standalone: true,
   imports: [RegistrationFormComponent],
   template: `
    <div>
      <app-registration-form <error descr="TS2739: Type '{}' is missing the following properties from type 'Product': id, title, description">[product]</error>="{}"></app-registration-form>
      <app-registration-form <error descr="TS2739: Type '{ title: string; }' is missing the following properties from type 'Product': id, description">[product]</error>="{title,}"></app-registration-form>
      <app-registration-form <error descr="TS2741: Property 'id' is missing in type '{ title: string; description: string; }' but required in type 'Product'.">[product]</error>="{title, description: 'foo'}"></app-registration-form>
    </div>
  `,
 })
export class AppComponent {
  title!: string
}
