import {Component} from '@angular/core';

interface IData {
  icon?: string
}

@Component({
  selector: 'app-root',
  template: `
    {{ <error descr="TS2339: Property '$safeNavigationMigration' does not exist on type 'AppComponent'.">$safeNavigationMigration</error>(data?.icon) }}
  `,
  standalone: true
})
export class AppComponent {
  data: IData | undefined;
}
