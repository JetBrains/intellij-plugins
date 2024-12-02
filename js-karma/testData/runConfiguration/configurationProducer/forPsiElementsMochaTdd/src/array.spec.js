suite('Top level suite', () => {
  setup(() => {})
  suite('Subsuite', () => {
    test('Test in subsuite', () => {
      expect(1).to.equal(1)
    })
  })
})

test('Top level test', () => {
  expect(1).to.equal(1)
})
