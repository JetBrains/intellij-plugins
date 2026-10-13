import {Component} from '@angular/core';

interface IData {
  icon?: string
}

@Component({
  selector: 'app-root',
  template: `
    {{ $safeNavigationMigration(data?.icon) }}
    {{ $safeNavigationMigration(data?.icon)?.length }}
    {{ $safeNavigationMigration(data?.<error descr="TS2339: Property 'foo' does not exist on type 'IData'.">foo</error>) }}
    <button (click)="save($safeNavigationMigration(data?.icon))">save</button>
    <button (click)="saveNumber($safeNavigationMigration(<error descr="TS2345: Argument of type 'string | undefined' is not assignable to parameter of type 'number'.
  Type 'undefined' is not assignable to type 'number'.">data?.icon</error>))">save</button>
    <button (click)="saveNumber(<error descr="TS2345: Argument of type 'string | undefined' is not assignable to parameter of type 'number'.
  Type 'undefined' is not assignable to type 'number'.">data?.icon</error>)">save</button>
  `,
  standalone: true
})
export class AppComponent {
  data: IData | undefined;

  save(icon: string | undefined) {
    return icon;
  }

  saveNumber(icon: number) {
    return icon;
  }
}
