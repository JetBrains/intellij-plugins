Widget buildDeeplyNestedWidget(BuildContext context) {
  // This structure is intentionally and excessively nested to 28 levels
  return ( // Level 1
      Padding( // Level 2
          padding: (1.0),
          child: (Card( // Level 3
              elevation: (1.0),
              child: (Container( // Level 4
                  decoration: (MockDecoration(color: (MockColor(1,1,1,1)))),
                  child: (Column( // Level 5
                      children: [
                        (Padding( // Level 6
                            padding: (1.0),
                            child: (Container( // Level 7
                                child: (Row( // Level 8
                                    children: [
                                      (Flexible( // Level 9
                                          child: (Container( // Level 10
                                              child: (Padding( // Level 11
                                                  padding: (1.0),
                                                  child: (Container( // Level 12
                                                      child: (Padding( // Level 13
                                                          padding: (1.0),
                                                          child: (Container( // Level 14
                                                              child: (Padding( // Level 15
                                                                  padding: (1.0),
                                                                  child: (ElevatedButton( // Level 16
                                                                      child: (Text("Deep Button")),
                                                                      onPressed: (() { // Level 17
                                                                        print( // Level 18
                                                                            ("Event: " + ( // Level 19
                                                                                (100 / ( // Level 20
                                                                                    (5 * ( // Level 21
                                                                                        (1 + ( // Level 22
                                                                                            (2 - ( // Level 23
                                                                                                (3 / ( // Level 24
                                                                                                    (4 * ( // Level 25
                                                                                                        (5 + ( // Level 26
                                                                                                            (6 - 1) // Level 27
                                                                                                        )) // Level 26
                                                                                                    )) // Level 25
                                                                                                )) // Level 24
                                                                                            )) // Level 23
                                                                                        )) // Level 22
                                                                                    )) // Level 21
                                                                                )).toString() // Level 20
                                                                            )) // Level 19
                                                                        ); // Level 18
                                                                      }) // Level 17
                                                                  )) // Level 16
                                                              )) // Level 15
                                                          )) // Level 14
                                                      )) // Level 13
                                                  )) // Level 12
                                              )) // Level 11
                                          )) // Level 10
                                      )) // Level 9
                                    ]
                                )) // Level 8
                            )) // Level 7
                        )) // Level 6
                      ]
                  )) // Level 5
              )) // Level 4
          )) // Level 3
      ) // Level 2
  ); // Level 1
}