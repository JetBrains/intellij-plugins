import { PartialStateUpdater } from '@ngrx/signals';
import { EntityChanges, EntityId, EntityPredicate, EntityState, NamedEntityState, SelectEntityId } from '../models';
export declare function updateEntities<Collection extends string, State extends NamedEntityState<any, Collection>, Entity = State extends NamedEntityState<infer E, Collection> ? E : never>(update: {
    ids: EntityId[];
    changes: EntityChanges<NoInfer<Entity>>;
}, config: {
    collection: Collection;
    selectId: SelectEntityId<NoInfer<Entity>>;
}): PartialStateUpdater<State>;
export declare function updateEntities<Collection extends string, State extends NamedEntityState<any, Collection>, Entity = State extends NamedEntityState<infer E, Collection> ? E : never>(update: {
    predicate: EntityPredicate<Entity>;
    changes: EntityChanges<NoInfer<Entity>>;
}, config: {
    collection: Collection;
    selectId: SelectEntityId<NoInfer<Entity>>;
}): PartialStateUpdater<State>;
export declare function updateEntities<Collection extends string, State extends NamedEntityState<any, Collection>, Entity = State extends NamedEntityState<infer E extends {
    id: EntityId;
}, Collection> ? E : never>(update: {
    ids: EntityId[];
    changes: EntityChanges<NoInfer<Entity>>;
}, config: {
    collection: Collection;
}): PartialStateUpdater<State>;
export declare function updateEntities<Collection extends string, State extends NamedEntityState<any, Collection>, Entity = State extends NamedEntityState<infer E extends {
    id: EntityId;
}, Collection> ? E : never>(update: {
    predicate: EntityPredicate<Entity>;
    changes: EntityChanges<NoInfer<Entity>>;
}, config: {
    collection: Collection;
}): PartialStateUpdater<State>;
export declare function updateEntities<Entity>(update: {
    ids: EntityId[];
    changes: EntityChanges<NoInfer<Entity>>;
}, config: {
    selectId: SelectEntityId<NoInfer<Entity>>;
}): PartialStateUpdater<EntityState<Entity>>;
export declare function updateEntities<Entity>(update: {
    predicate: EntityPredicate<Entity>;
    changes: EntityChanges<NoInfer<Entity>>;
}, config: {
    selectId: SelectEntityId<NoInfer<Entity>>;
}): PartialStateUpdater<EntityState<Entity>>;
export declare function updateEntities<Entity extends {
    id: EntityId;
}>(update: {
    ids: EntityId[];
    changes: EntityChanges<NoInfer<Entity>>;
}): PartialStateUpdater<EntityState<Entity>>;
export declare function updateEntities<Entity extends {
    id: EntityId;
}>(update: {
    predicate: EntityPredicate<Entity>;
    changes: EntityChanges<NoInfer<Entity>>;
}): PartialStateUpdater<EntityState<Entity>>;
