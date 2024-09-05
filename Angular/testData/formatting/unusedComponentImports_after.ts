import {ChangeDetectionStrategy, Component} from '@angular/core';
import {
    AsyncPipe,
    CommonModule,
    CurrencyPipe,
    DatePipe,
    DecimalPipe,
    JsonPipe,
    NgForOf,
    PercentPipe,
} from '@angular/common';

@Component({
    standalone: true,
    selector: 'cdt-settings',
    templateUrl: './unusedComponentImports.html',
    imports: [
        CurrencyPipe, // incorrectly used in event binding
        DatePipe, // used in an interpolation
        JsonPipe, // used in property binding expression
        PercentPipe, // used in template binding expr
        NgForOf, // template directive
        DecimalPipe, //used in block expression
        CommonModule, // should not be optimized
    ],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SettingsComponent {
}

@Component({
    standalone: true,
    selector: 'cdt-settings',
    template: `
    <div (click)="check(12 | async)" [title]="12 | json">
      {{ 12 | date}}
    </div>
    <div *ngFor="let item of [12 | percent, 13]">
      {{ item }}
    </div>
    @if (12 | number) {

    }
  `,
    imports: [
        AsyncPipe, // incorrectly used in event binding
        DatePipe, // used in an interpolation
        JsonPipe, // used in property binding expression
        PercentPipe, // used in template binding expr
        NgForOf, // template directive
        DecimalPipe, //used in block expression
        CommonModule, // should not be optimized
    ],
    changeDetection: ChangeDetectionStrategy.OnPush,
})
export class SettingsComponent2 {
}