/**
 * This file is part of Everit - HTML form-based authentication.
 *
 * Everit - HTML form-based authentication is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Everit - HTML form-based authentication is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Everit - HTML form-based authentication.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.everit.osgi.authentication.form.internal;

import java.io.IOException;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.everit.osgi.authentication.context.AuthenticationPropagator;
import org.everit.osgi.authenticator.Authenticator;
import org.everit.osgi.resource.resolver.ResourceIdResolver;
import org.osgi.service.log.LogService;

public class FormAuthenticationServlet extends HttpServlet {

    private static final long serialVersionUID = 2393726333850909710L;

    private final Authenticator authenticator;

    private final ResourceIdResolver resourceIdResolver;

    private final LogService logService;

    private final String formParamNameUsername;

    private final String formParamNamePassword;

    private final String formParamNameLastAccessedUrl;

    private final String loginUrl;

    public FormAuthenticationServlet(final Authenticator authenticator, final ResourceIdResolver resourceIdResolver,
            final AuthenticationPropagator authenticationPropagator, final LogService logService,
            final String formParamNameUsername, final String formParamNamePassword,
            final String formParamNameLastAccessedUrl, final String loginUrl) {
        super();
        this.authenticator = authenticator;
        this.resourceIdResolver = resourceIdResolver;
        this.logService = logService;
        this.formParamNameUsername = formParamNameUsername;
        this.formParamNamePassword = formParamNamePassword;
        this.formParamNameLastAccessedUrl = formParamNameLastAccessedUrl;
        this.loginUrl = loginUrl;
    }

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException,
            IOException {

        // Get authentication form parameters from the request
        String username = req.getParameter(formParamNameUsername);
        String password = req.getParameter(formParamNamePassword);

        // Authentication
        String authenticatedPrincipal = authenticator.authenticate(username, password);
        if (authenticatedPrincipal == null) {
            logService.log(LogService.LOG_INFO, "Failed to authenticate username '" + username + "'.");
            redirectToLoginUrl(req, resp);
            return;
        }

        // Resource ID mapping
        Long authenticatedResourceId = resourceIdResolver.getResourceId(authenticatedPrincipal);
        if (authenticatedResourceId == null) {
            logService.log(LogService.LOG_INFO, "Authenticated username '" + username
                    + "' (aka mapped principal '" + authenticatedPrincipal + "') cannot be mapped to Resource ID");
            redirectToLoginUrl(req, resp);
            return;
        }

        // Store the resource ID in the session
        HttpSession httpSession = req.getSession();
        httpSession.setAttribute(AuthenticationFilter.AUTHENTICATED_RESOURCE_ID, authenticatedResourceId);

        redirectToLastAccessedUrl(req, resp);
    }

    private void redirectToLastAccessedUrl(final HttpServletRequest req, final HttpServletResponse resp)
            throws IOException {
        String lastAccessedUrl = req.getParameter(formParamNameLastAccessedUrl);
        if ((lastAccessedUrl == null) || lastAccessedUrl.isEmpty()) {
            lastAccessedUrl = loginUrl;
        }
        // TODO URL encode/decode
        resp.sendRedirect(lastAccessedUrl);
    }

    private void redirectToLoginUrl(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        String lastAccessedUrl = req.getParameter(formParamNameLastAccessedUrl);
        String redirectUrl = loginUrl;
        if ((lastAccessedUrl != null) && !lastAccessedUrl.isEmpty()) {
            redirectUrl = redirectUrl + "?" + formParamNameLastAccessedUrl + "=" + lastAccessedUrl;
        }
        // TODO URL encode/decode
        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        resp.sendRedirect(redirectUrl);
    }

}
