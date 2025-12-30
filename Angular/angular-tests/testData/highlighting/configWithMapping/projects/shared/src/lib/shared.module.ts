import { NgModule } from '@angular/core';
import { SharedComponent } from './shared.component';
import { NgForOf } from '@angular/common';
import { ButtonComponent } from './button.component';


@NgModule({
  declarations: [
    ButtonComponent,
    SharedComponent,
  ],
  imports: [
    NgForOf,
  ],
  exports: [
    ButtonComponent,
    SharedComponent,
  ],
})
export class SharedModule {
}
