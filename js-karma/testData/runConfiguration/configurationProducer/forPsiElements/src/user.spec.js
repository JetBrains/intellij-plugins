describe('user', () => {
  it('should be tested', () => {
    expect(true).toBe(true)
  })

  describe('subsuite', () => {
    beforeEach(() => console.log('1'))
    it('test in subsuite', () => {
      expect(true).toBe(true)
    })
  })
})

it(`test without suite`, () => {
  expect(true).toBe(true)
})
