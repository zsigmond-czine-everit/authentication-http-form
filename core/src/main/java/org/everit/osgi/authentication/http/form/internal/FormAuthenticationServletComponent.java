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
package org.everit.osgi.authentication.http.form.internal;

import java.io.IOException;
import java.util.Map;
import java.util.Optional;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.everit.osgi.authentication.context.AuthenticationPropagator;
import org.everit.osgi.authentication.http.form.FormAuthenticationServletConstants;
import org.everit.osgi.authentication.http.session.AuthenticationSessionAttributeNames;
import org.everit.osgi.authenticator.Authenticator;
import org.everit.osgi.resource.resolver.ResourceIdResolver;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.log.LogService;

@Component(name = FormAuthenticationServletConstants.SERVICE_FACTORYPID_FORM_AUTHENTICATION_SERVLET, metatype = true,
        configurationFactory = true, policy = ConfigurationPolicy.REQUIRE, immediate = true)
@Properties({
        @Property(name = Constants.SERVICE_DESCRIPTION, propertyPrivate = false,
                value = FormAuthenticationServletConstants.DEFAULT_SERVICE_DESCRIPTION),
        @Property(name = FormAuthenticationServletConstants.PROP_FORM_PARAM_NAME_USERNAME,
                value = FormAuthenticationServletConstants.DEFAULT_FORM_PARAM_NAME_USERNAME),
        @Property(name = FormAuthenticationServletConstants.PROP_FORM_PARAM_NAME_PASSWORD,
                value = FormAuthenticationServletConstants.DEFAULT_FORM_PARAM_NAME_PASSWORD),
        @Property(name = FormAuthenticationServletConstants.PROP_SUCCESS_URL,
                value = FormAuthenticationServletConstants.DEFAULT_SUCCESS_URL),
        @Property(name = FormAuthenticationServletConstants.PROP_FORM_PARAM_NAME_SUCCESS_URL,
                value = FormAuthenticationServletConstants.DEFAULT_FORM_PARAM_NAME_SUCCESS_URL),
        @Property(name = FormAuthenticationServletConstants.PROP_FAILED_URL,
                value = FormAuthenticationServletConstants.DEFAULT_FAILED_URL),
        @Property(name = FormAuthenticationServletConstants.PROP_FORM_PARAM_NAME_FAILED_URL,
                value = FormAuthenticationServletConstants.DEFAULT_FORM_PARAM_NAME_FAILED_URL),
        @Property(name = FormAuthenticationServletConstants.PROP_AUTHENTICATOR),
        @Property(name = FormAuthenticationServletConstants.PROP_RESOURCE_ID_RESOLVER),
        @Property(name = FormAuthenticationServletConstants.PROP_AUTHENTICATION_PROPAGATOR),
        @Property(name = FormAuthenticationServletConstants.PROP_AUTHENTICATION_SESSION_ATTRIBUTE_NAMES),
        @Property(name = FormAuthenticationServletConstants.PROP_LOG_SERVICE),
})
@Service
public class FormAuthenticationServletComponent extends HttpServlet {

    private static final long serialVersionUID = -7849263592022625334L;

    @Reference(bind = "setAuthenticator")
    private Authenticator authenticator;

    @Reference(bind = "setResourceIdResolver")
    private ResourceIdResolver resourceIdResolver;

    @Reference(bind = "setAuthenticationPropagator")
    private AuthenticationPropagator authenticationPropagator;

    @Reference(bind = "setAuthenticationSessionAttributeNames")
    private AuthenticationSessionAttributeNames authenticationSessionAttributeNames;

    @Reference(bind = "setLogService")
    private LogService logService;

    private String formParamNameUsername;

    private String formParamNamePassword;

    private String successUrl;

    private String formParamNameSuccessUrl;

    private String failedUrl;

    private String formParamNameFailedUrl;

    @Activate
    public void activate(final BundleContext context, final Map<String, Object> componentProperties) throws Exception {
        formParamNameUsername = getStringProperty(componentProperties,
                FormAuthenticationServletConstants.PROP_FORM_PARAM_NAME_USERNAME);
        formParamNamePassword = getStringProperty(componentProperties,
                FormAuthenticationServletConstants.PROP_FORM_PARAM_NAME_PASSWORD);
        successUrl = getStringProperty(componentProperties,
                FormAuthenticationServletConstants.PROP_SUCCESS_URL);
        formParamNameSuccessUrl = getStringProperty(componentProperties,
                FormAuthenticationServletConstants.PROP_FORM_PARAM_NAME_SUCCESS_URL);
        failedUrl = getStringProperty(componentProperties,
                FormAuthenticationServletConstants.PROP_FAILED_URL);
        formParamNameFailedUrl = getStringProperty(componentProperties,
                FormAuthenticationServletConstants.PROP_FORM_PARAM_NAME_FAILED_URL);
    }

    @Override
    protected void doPost(final HttpServletRequest req, final HttpServletResponse resp) throws ServletException,
            IOException {
        // Get authentication form parameters from the request
        String username = req.getParameter(formParamNameUsername);
        String password = req.getParameter(formParamNamePassword);

        // Authentication
        Optional<String> optionalAuthenticatedPrincipal = authenticator.authenticate(username, password);
        if (!optionalAuthenticatedPrincipal.isPresent()) {
            logService.log(LogService.LOG_INFO, "Failed to authenticate username '" + username + "'.");
            redirectToFailedUrl(req, resp);
            return;
        }

        // Resource ID mapping
        String authenticatedPrincipal = optionalAuthenticatedPrincipal.get();
        Optional<Long> optionalAuthenticatedResourceId = resourceIdResolver.getResourceId(authenticatedPrincipal);
        if (!optionalAuthenticatedResourceId.isPresent()) {
            logService.log(LogService.LOG_INFO, "Authenticated username '" + username
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

    private String getStringProperty(final Map<String, Object> componentProperties, final String propertyName)
            throws ConfigurationException {
        Object value = componentProperties.get(propertyName);
        if (value == null) {
            throw new ConfigurationException(propertyName, "property not defined");
        }
        return String.valueOf(value);
    }

    private void redirectToFailedUrl(final HttpServletRequest req, final HttpServletResponse resp) throws IOException {
        String reqFailedUrl = req.getParameter(formParamNameFailedUrl);
        resp.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
        if (reqFailedUrl != null) {
            resp.sendRedirect(reqFailedUrl);
        } else {
            resp.sendRedirect(failedUrl);
        }
    }

    private void redirectToSuccessUrl(final HttpServletRequest req, final HttpServletResponse resp)
            throws IOException {
        String reqSuccessUrl = req.getParameter(formParamNameSuccessUrl);
        if (reqSuccessUrl != null) {
            resp.sendRedirect(reqSuccessUrl);
        } else {
            resp.sendRedirect(successUrl);
        }
    }

    public void setAuthenticationPropagator(final AuthenticationPropagator authenticationPropagator) {
        this.authenticationPropagator = authenticationPropagator;
    }

    public void setAuthenticationSessionAttributeNames(
            final AuthenticationSessionAttributeNames authenticationSessionAttributeNames) {
        this.authenticationSessionAttributeNames = authenticationSessionAttributeNames;
    }

    public void setAuthenticator(final Authenticator authenticator) {
        this.authenticator = authenticator;
    }

    public void setLogService(final LogService logService) {
        this.logService = logService;
    }

    public void setResourceIdResolver(final ResourceIdResolver resourceIdResolver) {
        this.resourceIdResolver = resourceIdResolver;
    }

}
