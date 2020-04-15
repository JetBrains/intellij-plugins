import { StateFactory } from '../internal/state-factory';
import { InternalStateOperations } from '../internal/state-operations';
import { Store } from '../store';
import { SelectFactory } from '../decorators/select/select-factory';
import { StateClassInternal } from '../internal/internals';
import { LifecycleStateManager } from '../internal/lifecycle-state-manager';
/**
 * Root module
 * @ignore
 */
export declare class NgxsRootModule {
    constructor(factory: StateFactory, internalStateOperations: InternalStateOperations, _store: Store, _select: SelectFactory, states: StateClassInternal<any, any>[] | undefined, lifecycleStateManager: LifecycleStateManager);
}
