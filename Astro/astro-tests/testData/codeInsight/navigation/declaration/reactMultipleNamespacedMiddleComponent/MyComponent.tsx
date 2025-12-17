import {NestedSecondLevelComponent} from "./NestedComponent"

function NestedComponent() {
  return (<div></div>);
}

NestedComponent.NestedSecondLevelComponent = NestedSecondLevelComponent

export const Components = {
  NestedComponent
}