import {Component, Input} from '@angular/core';

export type Product = {   id: string;   title: string;   description: string; };

@Component({
   selector: 'app-registration-form',
   template: './registration-form.component.html',
 })
export class RegistrationFormComponent {
  @Input()
  product!: Product
}

@Component({
   selector: 'app-root',
   imports: [RegistrationFormComponent],
   template: `
    <div>
      <app-registration-form [product]="{title}"></app-registration-form>
    </div>
  `,
   styleUrl: './app.component.css'
 })
export class AppComponent {
  title!: string
}
