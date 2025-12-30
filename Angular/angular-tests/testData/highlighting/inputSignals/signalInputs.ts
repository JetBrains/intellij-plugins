// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
import {Component, input} from '@angular/core';

@Component({
    selector: 'signal-inputs',
    template: ``,
    standalone: true
})
export class TodoCmp {

    inputNoParams = input()

    inputWithType = input<string>()

    inputWithInitialValue = input(12)

    inputWithAlias = input(12, {alias: "aliasedInput"})

    inputWithTransformTyped = input(12, {transform: (foo: string) => Number.parseInt(foo)})

    inputWithTransformUntyped = input(true, {transform: foo => foo === 12})

    inputWithTransformParametrized = input<number, string>(12, {transform: foo => Number.parseInt(foo)})

    inputRequired = input.required()

    inputRequiredAliased = input.required<string>({alias: "aliasedRequiredInput"})

}
