// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component, Pipe, PipeTransform} from "@angular/core";

@Component({
  selector: 'app-power-booster',
  template: `
        <h2>Power Booster</h2>
        <p>Super power boost: {{2 | exponentialStrength:<caret>10 }}</p>  
    `
})
export class PowerBoosterComponent { }

@Pipe({name: 'exponentialStrength'})
export class ExponentialStrengthPipe implements PipeTransform {
  transform(value: number, exponent: string): number {
    let exp = parseFloat(exponent);
    var q = String(1);
    var z = 10;
    return Math.pow(value, isNaN(exp) ? 1 : exp);
  }
}