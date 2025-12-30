import { PartialStateUpdater } from '@ngrx/signals';
import { EntityId, EntityPredicate, EntityState, NamedEntityState } from '../models';
export declare function removeEntities(ids: EntityId[]): PartialStateUpdater<EntityState<any>>;
export declare function removeEntities<Entity>(predicate: EntityPredicate<Entity>): PartialStateUpdater<EntityState<Entity>>;
export declare function removeEntities<Collection extends string>(ids: EntityId[], config: {
    collection: Collection;
}): PartialStateUpdater<NamedEntityState<any, Collection>>;
export declare function removeEntities<Collection extends string, State extends NamedEntityState<any, Collection>, Entity = State extends NamedEntityState<infer E, Collection> ? E : never>(predicate: EntityPredicate<Entity>, config: {
    collection: Collection;
}): PartialStateUpdater<State>;
