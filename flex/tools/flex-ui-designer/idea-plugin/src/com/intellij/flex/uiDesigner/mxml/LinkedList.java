package com.intellij.flex.uiDesigner.mxml;

class LinkedList<E> extends java.util.LinkedList<E> {
  public void addBefore(E current, E newElement) {
    final int currentIndex = indexOf(current);
    if (currentIndex == 0) {
      addFirst(newElement);
    }
    else {
      add(currentIndex - 1, newElement);
    }
  }
  
  public E getNext(E e) {
    final int nextIndex = indexOf(e) + 1;
    if (nextIndex == size()) {
      return null;
    }
    else {
      return get(nextIndex);
    }
  }
}
