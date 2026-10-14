import {computed, Injectable} from '@angular/core';

@Injectable()
export class MyService {
  readonly prop = computed(() => 'abc');
  readonly <warning descr="Unused readonly field unusedProp">unusedProp</warning> = computed(() => 'abc');
}
