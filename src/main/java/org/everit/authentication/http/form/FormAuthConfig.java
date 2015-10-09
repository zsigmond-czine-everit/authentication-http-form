/*
 * Copyright (C) 2011 Everit Kft. (http://www.everit.biz)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *         http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.everit.authentication.http.form;

/**
 * Parameters of the Form Authentication.
 */
public class FormAuthConfig {

  public final String failedUrl;

  public final String formParamNameFailedUrl;

  public final String formParamNamePassword;

  public final String formParamNameSuccessUrl;

  public final String formParamNameUsername;

  public final String successUrl;

  /**
   * Constructor.
   */
  public FormAuthConfig(final String formParamNameUsername,
      final String formParamNamePassword,
      final String successUrl, final String formParamNameSuccessUrl, final String failedUrl,
      final String formParamNameFailedUrl) {
    this.formParamNameUsername = formParamNameUsername;
    this.formParamNamePassword = formParamNamePassword;
    this.successUrl = successUrl;
    this.formParamNameSuccessUrl = formParamNameSuccessUrl;
    this.failedUrl = failedUrl;
    this.formParamNameFailedUrl = formParamNameFailedUrl;
  }

}
