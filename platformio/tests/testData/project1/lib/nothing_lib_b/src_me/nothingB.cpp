//
// Created by elmot on 7 Mar 2023.
//

#include "nothingB.h"
void doNothingB(void) {
#ifndef NOTHING_B_MANDATORY_DEFINE_B1
//nothing but fail
#error "NOTHING_B_MANDATORY_DEFINE_B1 is not defined"
#endif

#ifndef NOTHING_B_MANDATORY_DEFINE_B2
//nothing but fail
#error "NOTHING_B_MANDATORY_DEFINE_B2 is not defined"
#endif

}