// Copyright 2000-2023 JetBrains s.r.o. and contributors. Use of this source code is governed by the Apache 2.0 license.
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