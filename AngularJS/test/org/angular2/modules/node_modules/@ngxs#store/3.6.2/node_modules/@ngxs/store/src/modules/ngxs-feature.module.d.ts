import { Store } from '../store';
import { InternalStateOperations } from '../internal/state-operations';
import { StateFactory } from '../internal/state-factory';
import { LifecycleStateManager } from '../internal/lifecycle-state-manager';
import { StateClassInternal } from '../internal/internals';
/**
 * Feature module
 * @ignore
 */
export declare class NgxsFeatureModule {
    constructor(_store: Store, internalStateOperations: InternalStateOperations, factory: StateFactory, states: StateClassInternal<any, any>[][] | undefined, lifecycleStateManager: LifecycleStateManager);
    private static flattenStates;
}
