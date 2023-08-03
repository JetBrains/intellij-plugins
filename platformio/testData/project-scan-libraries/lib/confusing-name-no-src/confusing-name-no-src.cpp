#include "confusing-name-no-src.h"

#ifndef MANDATORY_DEFINE_B1
#error "Library Build flag -D is not working properly"
#endif

#ifndef MANDATORY_DEFINE_B2
#error "Library Build flag -D is not working properly"
#endif

void checkNoSrc() {
//Nothing to do

}