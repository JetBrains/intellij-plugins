const x = 1<caret><error descr="Standard code style: Extra semicolon. (semi)">;</error>
console.log(x)

function reactComponent () {
  return <div className=<error descr="Standard code style: Unexpected usage of doublequote. (jsx-quotes)">"App"</error> />
}

reactComponent()
