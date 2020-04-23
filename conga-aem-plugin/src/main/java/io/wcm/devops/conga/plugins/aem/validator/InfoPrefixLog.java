/*
 * #%L
 * wcm.io
 * %%
 * Copyright (C) 2020 wcm.io
 * %%
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 * #L%
 */
package io.wcm.devops.conga.plugins.aem.validator;

import org.apache.maven.plugin.logging.Log;

/**
 * Prefixes all info log messages with the given string.
 */
class InfoPrefixLog implements Log {

  private final Log delegate;
  private final String prefix;

  InfoPrefixLog(Log delegate, String prefix) {
    this.delegate = delegate;
    this.prefix = prefix;
  }

  @Override
  public boolean isDebugEnabled() {
    return this.delegate.isDebugEnabled();
  }

  @Override
  public void debug(CharSequence content) {
    this.delegate.debug(content);
  }

  @Override
  public void debug(CharSequence content, Throwable error) {
    this.delegate.debug(content, error);
  }

  @Override
  public void debug(Throwable error) {
    this.delegate.debug(error);
  }

  @Override
  public boolean isInfoEnabled() {
    return this.delegate.isInfoEnabled();
  }

  @Override
  public void info(CharSequence content) {
    this.delegate.info(prefix + content);
  }

  @Override
  public void info(CharSequence content, Throwable error) {
    this.delegate.info(prefix + content, error);
  }

  @Override
  public void info(Throwable error) {
    this.delegate.info(error);
  }

  @Override
  public boolean isWarnEnabled() {
    return this.delegate.isWarnEnabled();
  }

  @Override
  public void warn(CharSequence content) {
    this.delegate.warn(content);
  }

  @Override
  public void warn(CharSequence content, Throwable error) {
    this.delegate.warn(content, error);
  }

  @Override
  public void warn(Throwable error) {
    this.delegate.warn(error);
  }

  @Override
  public boolean isErrorEnabled() {
    return this.delegate.isErrorEnabled();
  }

  @Override
  public void error(CharSequence content) {
    this.delegate.error(content);
  }

  @Override
  public void error(CharSequence content, Throwable error) {
    this.delegate.error(content, error);
  }

  @Override
  public void error(Throwable error) {
    this.delegate.error(error);
  }

}
