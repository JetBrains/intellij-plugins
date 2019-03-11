/**
 * @license
 * Copyright Google Inc. All Rights Reserved.
 *
 * Use of this source code is governed by an MIT-style license that can be
 * found in the LICENSE file at https://angular.io/license
 */
import { Type } from '../type';
import { ClassSansProvider, ConstructorSansProvider, ExistingSansProvider, FactorySansProvider, StaticClassSansProvider, ValueSansProvider } from './provider';
export declare function convertInjectableProviderToFactory(type: Type<any>, provider?: ValueSansProvider | ExistingSansProvider | StaticClassSansProvider | ConstructorSansProvider | FactorySansProvider | ClassSansProvider): () => any;
