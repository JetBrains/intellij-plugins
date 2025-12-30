export interface BaseEntity {
  id: string;
  name: string;
}

export interface ChildEntity extends BaseEntity {
  type: string;
}

export interface EntityContainer<T> {
  contents: T[];
}
