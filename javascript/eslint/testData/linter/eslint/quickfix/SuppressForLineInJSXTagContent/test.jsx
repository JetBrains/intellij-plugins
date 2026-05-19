import React, { Component } from 'react';

class App extends Component {
  render() {
    return (
        <div className="App">
          <div className="App-header">
            <error descr="ESLint: Empty components are self-closing (react/self-closing-comp)"><caret><h2></error></h2>
          </div>
        </div>
    );
  }
}
