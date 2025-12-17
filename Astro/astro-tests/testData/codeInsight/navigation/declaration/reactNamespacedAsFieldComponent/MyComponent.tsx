function MyComponent() {
  return (<div></div>);
}

function NestedComponent() {
  return (<div></div>);
}

MyComponent.Nested = NestedComponent

export default MyComponent;