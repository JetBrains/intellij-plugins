import { Component } from '@angular/core';

import {RegistrationFormComponent} from './registration-form.component';

@Component({
 selector: 'app-root',
 imports: [RegistrationFormComponent],
    templateUrl: './extractComponentTemplate.html',
 styleUrl: './app.component.css'
})
export class AppComponent {
  title = 'untitled3';
}
