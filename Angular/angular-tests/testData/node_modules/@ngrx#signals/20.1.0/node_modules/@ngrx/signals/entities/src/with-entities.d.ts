import { EmptyFeatureResult, SignalStoreFeature } from '@ngrx/signals';
import { EntityProps, EntityState, NamedEntityProps, NamedEntityState } from './models';
export declare function withEntities<Entity>(): SignalStoreFeature<EmptyFeatureResult, {
    state: EntityState<Entity>;
    props: EntityProps<Entity>;
    methods: {};
}>;
export declare function withEntities<Entity, Collection extends string>(config: {
    entity: Entity;
    collection: Collection;
}): SignalStoreFeature<EmptyFeatureResult, {
    state: NamedEntityState<Entity, Collection>;
    props: NamedEntityProps<Entity, Collection>;
    methods: {};
}>;
export declare function withEntities<Entity>(config: {
    entity: Entity;
}): SignalStoreFeature<EmptyFeatureResult, {
    state: EntityState<Entity>;
    props: EntityProps<Entity>;
    methods: {};
}>;
