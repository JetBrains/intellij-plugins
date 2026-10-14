import {Component, inject} from '@angular/core';
import {MyService} from './my.service';

@Component({
  selector: 'app-root',
  templateUrl: './app.html',
  providers: [MyService]
})
export class App {
  readonly svc = inject(MyService);
}
