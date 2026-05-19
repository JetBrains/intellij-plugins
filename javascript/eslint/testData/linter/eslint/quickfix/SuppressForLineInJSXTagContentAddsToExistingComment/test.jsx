import React, { Component } from 'react';

class App extends Component {
  render() {
    return (
      <div className="App">
        <div className='App-header'>
          {/* eslint-disable-next-line react/self-closing-comp */}
          <div <error descr="ESLint: Unknown property 'class' found, use 'className' instead (react/no-unknown-property)">cla<caret>ss=""</error>></div>
        </div>
      </div>
    );
  }
}
