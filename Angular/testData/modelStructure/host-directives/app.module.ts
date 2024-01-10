import { NgModule } from '@angular/core';

import {SourceComponent} from "./test.component"
import {TestComponent as IvyTestComponent} from "ivytest"

@NgModule({
  declarations: [
    SourceComponent,
    IvyTestComponent
  ]
})
export class AppModule { }
