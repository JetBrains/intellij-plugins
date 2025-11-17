import { SelectEntityId } from './models';
export declare function entityConfig<Entity, Collection extends string>(config: {
    entity: Entity;
    collection: Collection;
    selectId: SelectEntityId<NoInfer<Entity>>;
}): typeof config;
export declare function entityConfig<Entity>(config: {
    entity: Entity;
    selectId: SelectEntityId<NoInfer<Entity>>;
}): typeof config;
export declare function entityConfig<Entity, Collection extends string>(config: {
    entity: Entity;
    collection: Collection;
}): typeof config;
export declare function entityConfig<Entity>(config: {
    entity: Entity;
}): typeof config;
