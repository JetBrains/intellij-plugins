import React, { Component } from 'react'

class App extends Component {
  render () {
    return (
      <div className="App">
        <div className=<error descr="ESLint: Unexpected usage of singlequote. (jsx-quotes)"><caret>'wrong quotes here'</error>>
          <h2>Welcome to React</h2>
        </div>
      </div>
    )
  }
}