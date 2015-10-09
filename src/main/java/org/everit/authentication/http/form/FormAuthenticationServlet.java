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

import java.io.IOException;
import java.util.Optional;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.everit.authentication.http.session.AuthenticationSessionAttributeNames;
import org.everit.authenticator.Authenticator;
import org.everit.resource.resolver.ResourceIdResolver;
import org.everit.web.servlet.HttpServlet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The implementation of the form authentication mechanism.
 */
public class FormAuthenticationServlet extends HttpServlet {

  private static final Logger LOGGER = LoggerFactory.getLogger(FormAuthenticationServlet.class);

  private final AuthenticationSessionAttributeNames authenticationSessionAttributeNames;

  private final Authenticator authenticator;

  private final FormAuthConfig formAuthConfig;

  private final ResourceIdResolver resourceIdResolver;

  /**
   * Constructor.
   */
  public FormAuthenticationServlet(
      final AuthenticationSessionAttributeNames authenticationSessionAttributeNames,
      final Authenticator authenticator,
      final ResourceIdResolver resourceIdResolver,
      final FormAuthConfig formAuthConfig) {
    super();
    this.authenticationSessionAttributeNames = authenticationSessionAttributeNames;
    this.authenticator = authenticator;
    this.resourceIdResolver = resourceIdResolver;
    this.formAuthConfig = formAuthConfig;
  }

  private void redirectToFailedUrl(final HttpServletRequest req, final HttpServletResponse resp)
      throws IOException {
    String failedUrl = req.getParameter(formAuthConfig.formParamNameFailedUrl);
    resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
    if (failedUrl == null) {
      failedUrl = formAuthConfig.failedUrl;
    }
    safeRedirect(resp, failedUrl);
  }

  private void redirectToSuccessUrl(final HttpServletRequest req, final HttpServletResponse resp)
      throws IOException {
    String successUrl = req.getParameter(formAuthConfig.formParamNameSuccessUrl);
    if (successUrl == null) {
      successUrl = formAuthConfig.successUrl;
    }
    safeRedirect(resp, successUrl);
  }

  private void safeRedirect(final HttpServletResponse resp, final String redirectUrl)
      throws IOException {
    try {
      resp.sendRedirect(redirectUrl);
    } catch (Exception e) {
      throw new IllegalStateException(e);
    }
  }

  @Override
  protected void service(final HttpServletRequest req, final HttpServletResponse resp)
      throws ServletException, IOException {

    // Get authentication form parameters from the request
    String username = req.getParameter(formAuthConfig.formParamNameUsername);
    String password = req.getParameter(formAuthConfig.formParamNamePassword);

    // Authentication
    Optional<String> optionalAuthenticatedPrincipal =
        authenticator.authenticate(username, password);
    if (!optionalAuthenticatedPrincipal.isPresent()) {
      LOGGER.info("Failed to authenticate username '" + username + "'.");
      redirectToFailedUrl(req, resp);
      return;
    }

    // Resource ID mapping
    String authenticatedPrincipal = optionalAuthenticatedPrincipal.get();
    Optional<Long> optionalAuthenticatedResourceId =
        resourceIdResolver.getResourceId(authenticatedPrincipal);
    if (!optionalAuthenticatedResourceId.isPresent()) {
      LOGGER.info("Authenticated username '" + username
          + "' (aka mapped principal '" + optionalAuthenticatedPrincipal
          + "') cannot be mapped to Resource ID");
      redirectToFailedUrl(req, resp);
      return;
    }

    // Store the resource ID in the session
    Long authenticatedResourceId = optionalAuthenticatedResourceId.get();
    HttpSession httpSession = req.getSession();
    httpSession.setAttribute(
        authenticationSessionAttributeNames.authenticatedResourceId(), authenticatedResourceId);

    redirectToSuccessUrl(req, resp);
  }

}
