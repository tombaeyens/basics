/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */

package ai.shape.basics.routerservlet;

import org.slf4j.Logger;
import org.slf4j.helpers.MarkerIgnoringBase;
import org.slf4j.spi.LocationAwareLogger;

public class HttpInterceptingLogger extends MarkerIgnoringBase {

  Logger delegate;
  HttpLogInterceptor httpLogInterceptor;

  public HttpInterceptingLogger(Logger delegate, HttpLogInterceptor httpLogInterceptor) {
    this.delegate = delegate;
    this.httpLogInterceptor = httpLogInterceptor;
  }

  @Override
  public String getName() {
    return delegate.getName();
  }

  @Override
  public boolean isTraceEnabled() {
    return delegate.isTraceEnabled();
  }
  @Override
  public void trace(String msg) {
    trace(msg, (Throwable) null);
  }
  @Override
  public void trace(String format, Object param1) {
    trace(String.format(format, param1), (Throwable)null);
  }
  @Override
  public void trace(String format, Object param1, Object param2) {
    trace(String.format(format, param1, param2), (Throwable)null);
  }
  @Override
  public void trace(String format, Object... argArray) {
    trace(String.format(format, argArray), (Throwable)null);
  }
  @Override
  public void trace(String msg, Throwable t) {
    httpLogInterceptor.intercept(LocationAwareLogger.TRACE_INT, msg, t);
    delegate.trace(msg, t);
  }

  @Override
  public boolean isDebugEnabled() {
    return delegate.isDebugEnabled();
  }
  @Override
  public void debug(String msg) {
    debug(msg, (Throwable) null);
  }
  @Override
  public void debug(String format, Object param1) {
    debug(String.format(format, param1), (Throwable)null);
  }
  @Override
  public void debug(String format, Object param1, Object param2) {
    debug(String.format(format, param1, param2), (Throwable)null);
  }
  @Override
  public void debug(String format, Object... argArray) {
    debug(String.format(format, argArray), (Throwable)null);
  }
  @Override
  public void debug(String msg, Throwable t) {
    httpLogInterceptor.intercept(LocationAwareLogger.DEBUG_INT, msg, t);
    delegate.debug(msg, t);
  }

  @Override
  public boolean isInfoEnabled() {
    return delegate.isInfoEnabled();
  }
  @Override
  public void info(String msg) {
    info(msg, (Throwable) null);
  }
  @Override
  public void info(String format, Object param1) {
    info(String.format(format, param1), (Throwable)null);
  }
  @Override
  public void info(String format, Object param1, Object param2) {
    info(String.format(format, param1, param2), (Throwable)null);
  }
  @Override
  public void info(String format, Object... argArray) {
    info(String.format(format, argArray), (Throwable)null);
  }
  @Override
  public void info(String msg, Throwable t) {
    httpLogInterceptor.intercept(LocationAwareLogger.INFO_INT, msg, t);
    delegate.info(msg, t);
  }

  @Override
  public boolean isWarnEnabled() {
    return delegate.isWarnEnabled();
  }
  @Override
  public void warn(String msg) {
    warn(msg, (Throwable) null);
  }
  @Override
  public void warn(String format, Object param1) {
    warn(String.format(format, param1), (Throwable)null);
  }
  @Override
  public void warn(String format, Object param1, Object param2) {
    warn(String.format(format, param1, param2), (Throwable)null);
  }
  @Override
  public void warn(String format, Object... argArray) {
    warn(String.format(format, argArray), (Throwable)null);
  }
  @Override
  public void warn(String msg, Throwable t) {
    httpLogInterceptor.intercept(LocationAwareLogger.WARN_INT, msg, t);
    delegate.warn(msg, t);
  }


  @Override
  public boolean isErrorEnabled() {
    return delegate.isErrorEnabled();
  }
  @Override
  public void error(String msg) {
    error(msg, (Throwable) null);
  }
  @Override
  public void error(String format, Object param1) {
    error(String.format(format, param1), (Throwable)null);
  }
  @Override
  public void error(String format, Object param1, Object param2) {
    error(String.format(format, param1, param2), (Throwable)null);
  }
  @Override
  public void error(String format, Object... argArray) {
    error(String.format(format, argArray), (Throwable)null);
  }
  @Override
  public void error(String msg, Throwable t) {
    httpLogInterceptor.intercept(LocationAwareLogger.ERROR_INT, msg, t);
    delegate.error(msg, t);
  }
}
