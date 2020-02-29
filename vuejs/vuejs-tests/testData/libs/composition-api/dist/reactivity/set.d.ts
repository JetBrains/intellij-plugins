/**
 * Set a property on an object. Adds the new property, triggers change
 * notification and intercept it's subsequent access if the property doesn't
 * already exist.
 */
export declare function set<T>(target: any, key: any, val: T): T;
