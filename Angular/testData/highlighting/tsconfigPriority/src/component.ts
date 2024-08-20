import { ChangeDetectionStrategy, Component, OnDestroy, inject, Signal } from '@angular/core';

export interface Errors {
  [key: string]: string;
}

@Component({
  selector: 'cdt-list-errors',
  standalone: true,
  templateUrl: './component.html',
  changeDetection: ChangeDetectionStrategy.OnPush,
})
export class AppComponent implements OnDestroy {
  protected readonly formErrorsStore!: { errors: Signal<string[]>, setErrors: (errors: Errors) => void }

  ngOnDestroy() {
    this.formErrorsStore.setErrors({});
  }
}
