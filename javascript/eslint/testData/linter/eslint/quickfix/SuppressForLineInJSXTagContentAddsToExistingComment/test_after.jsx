import React, { Component } from 'react';

class App extends Component {
  render() {
    return (
      <div className="App">
        <div className='App-header'>
          {/* eslint-disable-next-line react/self-closing-comp,react/no-unknown-property */}
          <div cla<caret>ss=""></div>
        </div>
      </div>
    );
  }
}
