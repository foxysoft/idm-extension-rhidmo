/*******************************************************************************
 * Copyright 2022 Lambert Giese
 * 
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not
 * use this file except in compliance with the License.  You may obtain a copy
 * of the License at
 * 
 *   http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS, WITHOUT
 * WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.  See the
 * License for the specific language governing permissions and limitations under
 * the License.
 ******************************************************************************/
package de.foxysoft.rhidmo.test;

import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import org.junit.Test;
import org.slf4j.Logger;
import de.foxysoft.rhidmo.Log;
import de.foxysoft.rhidmo.mock.Slf4jNopLogger;

public class TestLog {

  public static final String LOG_MSG = "message";
  public static final Object LOG_ARGS = new Object[] {new Object()};
  public static final Throwable LOG_THROWABLE = new Exception();

  private static class LogWithLoggerSpy extends Log {
    protected LogWithLoggerSpy(Logger logger) {
      super(logger);
    }

    public Logger getLoggerSpy() {
      return m_logger;
    }

    public static LogWithLoggerSpy newInstance() {
      Logger slf4jLoggerSpy = spy(new Slf4jNopLogger());
      return new LogWithLoggerSpy(slf4jLoggerSpy);
    }

  }

  @Test
  public void testErrorThrowableCallsErrorString() throws Exception {
    LogWithLoggerSpy log = LogWithLoggerSpy.newInstance();
    log.error(LOG_THROWABLE);
    // The expected string is a strack trace, but to keep things simple,
    // we only test it contains the name of the class that generated the exception
    verify(log.getLoggerSpy()).error(contains(this.getClass().getName()));
  }

  @Test
  public void testErrorStringCallsErrorString() throws Exception {
    LogWithLoggerSpy log = LogWithLoggerSpy.newInstance();
    log.error(LOG_MSG);
    verify(log.getLoggerSpy()).error(LOG_MSG);
  }

  @Test
  public void testErrorStringObjectCallsErrorStringObject() throws Exception {
    LogWithLoggerSpy log = LogWithLoggerSpy.newInstance();
    log.error(LOG_MSG, LOG_ARGS);
    // Need to cast return value of eq for LOG_ARGS, otherwise runtime test failure
    verify(log.getLoggerSpy()).error(eq(LOG_MSG), (Object[]) eq(LOG_ARGS));
  }

  @Test
  public void testWarnStringCallsWarnString() throws Exception {
    LogWithLoggerSpy log = LogWithLoggerSpy.newInstance();
    log.warn(LOG_MSG);
    verify(log.getLoggerSpy()).warn(LOG_MSG);
  }

  @Test
  public void testWarnStringObjectCallsWarnStringObject() throws Exception {
    LogWithLoggerSpy log = LogWithLoggerSpy.newInstance();
    log.warn(LOG_MSG, LOG_ARGS);
    // Need to cast return value of eq for LOG_ARGS, otherwise runtime test failure
    verify(log.getLoggerSpy()).warn(eq(LOG_MSG), (Object[]) eq(LOG_ARGS));
  }

  @Test
  public void testDebugStringCallsDebugString() throws Exception {
    LogWithLoggerSpy log = LogWithLoggerSpy.newInstance();
    log.debug(LOG_MSG);
    verify(log.getLoggerSpy()).debug(LOG_MSG);
  }

  @Test
  public void testDebugStringObjectCallsDebugStringObject() throws Exception {
    LogWithLoggerSpy log = LogWithLoggerSpy.newInstance();
    log.debug(LOG_MSG, LOG_ARGS);
    // Need to cast return value of eq for LOG_ARGS, otherwise runtime test failure
    verify(log.getLoggerSpy()).debug(eq(LOG_MSG), (Object[]) eq(LOG_ARGS));
  }
}
