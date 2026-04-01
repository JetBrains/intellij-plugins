cmake_minimum_required(VERSION 3.25)
project(cmake-broken-test)

find_package(QodanaTestNonExistent REQUIRED)
add_executable(main main.cpp)
