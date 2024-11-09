/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (https://www.swig.org).
 * Version 4.1.0
 *
 * Do not make changes to this file unless you know what you are doing - modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package org.pjsip.pjsua2;

public class AuthCredInfoVector extends java.util.AbstractList<AuthCredInfo> implements java.util.RandomAccess {
  private transient long swigCPtr;
  protected transient boolean swigCMemOwn;

  protected AuthCredInfoVector(long cPtr, boolean cMemoryOwn) {
    swigCMemOwn = cMemoryOwn;
    swigCPtr = cPtr;
  }

  protected static long getCPtr(AuthCredInfoVector obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected static long swigRelease(AuthCredInfoVector obj) {
    long ptr = 0;
    if (obj != null) {
      if (!obj.swigCMemOwn)
        throw new RuntimeException("Cannot release ownership as memory is not owned");
      ptr = obj.swigCPtr;
      obj.swigCMemOwn = false;
      obj.delete();
    }
    return ptr;
  }

  @SuppressWarnings("deprecation")
  protected void finalize() {
    delete();
  }

  public synchronized void delete() {
    if (swigCPtr != 0) {
      if (swigCMemOwn) {
        swigCMemOwn = false;
        pjsua2JNI.delete_AuthCredInfoVector(swigCPtr);
      }
      swigCPtr = 0;
    }
  }

  public AuthCredInfoVector(AuthCredInfo[] initialElements) {
    this();
    reserve(initialElements.length);

    for (AuthCredInfo element : initialElements) {
      add(element);
    }
  }

  public AuthCredInfoVector(Iterable<AuthCredInfo> initialElements) {
    this();
    for (AuthCredInfo element : initialElements) {
      add(element);
    }
  }

  public AuthCredInfo get(int index) {
    return doGet(index);
  }

  public AuthCredInfo set(int index, AuthCredInfo e) {
    return doSet(index, e);
  }

  public boolean add(AuthCredInfo e) {
    modCount++;
    doAdd(e);
    return true;
  }

  public void add(int index, AuthCredInfo e) {
    modCount++;
    doAdd(index, e);
  }

  public AuthCredInfo remove(int index) {
    modCount++;
    return doRemove(index);
  }

  protected void removeRange(int fromIndex, int toIndex) {
    modCount++;
    doRemoveRange(fromIndex, toIndex);
  }

  public int size() {
    return doSize();
  }

  public AuthCredInfoVector() {
    this(pjsua2JNI.new_AuthCredInfoVector__SWIG_0(), true);
  }

  public AuthCredInfoVector(AuthCredInfoVector other) {
    this(pjsua2JNI.new_AuthCredInfoVector__SWIG_1(AuthCredInfoVector.getCPtr(other), other), true);
  }

  public long capacity() {
    return pjsua2JNI.AuthCredInfoVector_capacity(swigCPtr, this);
  }

  public void reserve(long n) {
    pjsua2JNI.AuthCredInfoVector_reserve(swigCPtr, this, n);
  }

  public boolean isEmpty() {
    return pjsua2JNI.AuthCredInfoVector_isEmpty(swigCPtr, this);
  }

  public void clear() {
    pjsua2JNI.AuthCredInfoVector_clear(swigCPtr, this);
  }

  public AuthCredInfoVector(int count, AuthCredInfo value) {
    this(pjsua2JNI.new_AuthCredInfoVector__SWIG_2(count, AuthCredInfo.getCPtr(value), value), true);
  }

  private int doSize() {
    return pjsua2JNI.AuthCredInfoVector_doSize(swigCPtr, this);
  }

  private void doAdd(AuthCredInfo x) {
    pjsua2JNI.AuthCredInfoVector_doAdd__SWIG_0(swigCPtr, this, AuthCredInfo.getCPtr(x), x);
  }

  private void doAdd(int index, AuthCredInfo x) {
    pjsua2JNI.AuthCredInfoVector_doAdd__SWIG_1(swigCPtr, this, index, AuthCredInfo.getCPtr(x), x);
  }

  private AuthCredInfo doRemove(int index) {
    return new AuthCredInfo(pjsua2JNI.AuthCredInfoVector_doRemove(swigCPtr, this, index), true);
  }

  private AuthCredInfo doGet(int index) {
    return new AuthCredInfo(pjsua2JNI.AuthCredInfoVector_doGet(swigCPtr, this, index), false);
  }

  private AuthCredInfo doSet(int index, AuthCredInfo val) {
    return new AuthCredInfo(pjsua2JNI.AuthCredInfoVector_doSet(swigCPtr, this, index, AuthCredInfo.getCPtr(val), val), true);
  }

  private void doRemoveRange(int fromIndex, int toIndex) {
    pjsua2JNI.AuthCredInfoVector_doRemoveRange(swigCPtr, this, fromIndex, toIndex);
  }

}
