// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component, HostBinding} from '@angular/core';

@Component({
  selector: 'app-root',
  standalone: true,
  templateUrl: "./component.html",
  host: {
    "[style.--test3]": "'#ffd000'",
  }
})
export class AppComponent {
  @HostBinding("style.--test4")
  foo: string = "bar";

}
