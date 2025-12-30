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
      <app-registration-form [product]="{}"></app-registration-form>
      <app-registration-form [product]="{title,}"></app-registration-form>
    </div>
  `,
 })
export class AppComponent {
  title!: string
}
