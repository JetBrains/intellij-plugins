// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component, Pipe, PipeTransform} from "@angular/core";


@Component({
  selector: 'app-power-booster',
  templateUrl: 'ParameterHintsInHtml.html'
})
export class PowerBoosterComponent {
  public foo(number: number, number2: number) {

  }
}

@Pipe({name: 'exponentialStrength'})
export class ExponentialStrengthPipe implements PipeTransform {
  transform(value: number, exponent: string): number {
    let exp = parseFloat(exponent);
    var q = String(<hint text="value:"/>1);
    var z = 10;
    return Math.pow(value, <hint text="y:"/>isNaN(exp) ? 1 : exp);
  }
}