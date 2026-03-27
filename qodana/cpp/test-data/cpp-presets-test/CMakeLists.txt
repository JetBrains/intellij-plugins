cmake_minimum_required(VERSION 3.25)
project(temp)

file(GLOB SOURCES *.cpp)
add_executable(main ${SOURCES})

if(BUGS)
	target_compile_definitions(main PRIVATE BUGS=1)
endif()
