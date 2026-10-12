import {Component} from '@angular/core';

interface IData {
  icon?: string
  check?(): string
}

@Component({
  selector: 'app-root',
  template: `
    <i class="i-{{ $safeNavigationMigration(data?.icon?.toString()) }}">data</i>
    <i [title]="$safeNavigationMigration(data?.check?.())">data</i>
    <button (click)="save($safeNavigationMigration(data?.icon))">save</button>
  `,
  standalone: true
})
export class AppComponent {
  data: IData | undefined;

  save(icon: string | undefined) {}
}
