/* ----------------------------------------------------------------------------
 * This file was automatically generated by SWIG (https://www.swig.org).
 * Version 4.1.0
 *
 * Do not make changes to this file unless you know what you are doing - modify
 * the SWIG interface file instead.
 * ----------------------------------------------------------------------------- */

package org.pjsip.pjsua2;

public class LogConfig extends PersistentObject {
  private transient long swigCPtr;

  protected LogConfig(long cPtr, boolean cMemoryOwn) {
    super(pjsua2JNI.LogConfig_SWIGUpcast(cPtr), cMemoryOwn);
    swigCPtr = cPtr;
  }

  protected static long getCPtr(LogConfig obj) {
    return (obj == null) ? 0 : obj.swigCPtr;
  }

  protected static long swigRelease(LogConfig obj) {
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
        pjsua2JNI.delete_LogConfig(swigCPtr);
      }
      swigCPtr = 0;
    }
    super.delete();
  }

  public void setMsgLogging(long value) {
    pjsua2JNI.LogConfig_msgLogging_set(swigCPtr, this, value);
  }

  public long getMsgLogging() {
    return pjsua2JNI.LogConfig_msgLogging_get(swigCPtr, this);
  }

  public void setLevel(long value) {
    pjsua2JNI.LogConfig_level_set(swigCPtr, this, value);
  }

  public long getLevel() {
    return pjsua2JNI.LogConfig_level_get(swigCPtr, this);
  }

  public void setConsoleLevel(long value) {
    pjsua2JNI.LogConfig_consoleLevel_set(swigCPtr, this, value);
  }

  public long getConsoleLevel() {
    return pjsua2JNI.LogConfig_consoleLevel_get(swigCPtr, this);
  }

  public void setDecor(long value) {
    pjsua2JNI.LogConfig_decor_set(swigCPtr, this, value);
  }

  public long getDecor() {
    return pjsua2JNI.LogConfig_decor_get(swigCPtr, this);
  }

  public void setFilename(String value) {
    pjsua2JNI.LogConfig_filename_set(swigCPtr, this, value);
  }

  public String getFilename() {
    return pjsua2JNI.LogConfig_filename_get(swigCPtr, this);
  }

  public void setFileFlags(long value) {
    pjsua2JNI.LogConfig_fileFlags_set(swigCPtr, this, value);
  }

  public long getFileFlags() {
    return pjsua2JNI.LogConfig_fileFlags_get(swigCPtr, this);
  }

  public void setWriter(LogWriter value) {
    pjsua2JNI.LogConfig_writer_set(swigCPtr, this, LogWriter.getCPtr(value), value);
  }

  public LogWriter getWriter() {
    long cPtr = pjsua2JNI.LogConfig_writer_get(swigCPtr, this);
    return (cPtr == 0) ? null : new LogWriter(cPtr, false);
  }

  public LogConfig() {
    this(pjsua2JNI.new_LogConfig(), true);
  }

  public void readObject(ContainerNode node) throws Exception {
    pjsua2JNI.LogConfig_readObject(swigCPtr, this, ContainerNode.getCPtr(node), node);
  }

  public void writeObject(ContainerNode node) throws Exception {
    pjsua2JNI.LogConfig_writeObject(swigCPtr, this, ContainerNode.getCPtr(node), node);
  }

}
