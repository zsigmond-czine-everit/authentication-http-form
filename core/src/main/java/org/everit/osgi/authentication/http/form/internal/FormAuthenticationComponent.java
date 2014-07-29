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

import java.util.Dictionary;
import java.util.Hashtable;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.Servlet;

import org.apache.felix.http.whiteboard.HttpWhiteboardConstants;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.everit.osgi.authentication.context.AuthenticationContext;
import org.everit.osgi.authentication.context.AuthenticationPropagator;
import org.everit.osgi.authentication.http.form.FormAuthenticationConstants;
import org.everit.osgi.authenticator.Authenticator;
import org.everit.osgi.resource.resolver.ResourceIdResolver;
import org.osgi.framework.BundleContext;
import org.osgi.framework.Constants;
import org.osgi.framework.ServiceRegistration;
import org.osgi.service.cm.ConfigurationException;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.log.LogService;

@Component(name = FormAuthenticationConstants.SERVICE_FACTORYPID_FORM_AUTHENTICATION, metatype = true,
        configurationFactory = true, policy = ConfigurationPolicy.REQUIRE)
@Properties({
        @Property(name = FormAuthenticationConstants.PROP_FILTER_NAME,
                value = FormAuthenticationConstants.DEFAULT_FILTER_NAME),
        @Property(name = HttpWhiteboardConstants.PATTERN,
                value = FormAuthenticationConstants.DEFAULT_PATTERN),
        @Property(name = HttpWhiteboardConstants.ALIAS,
                value = FormAuthenticationConstants.DEFAULT_ALIAS),
        @Property(name = HttpWhiteboardConstants.CONTEXT_ID,
                value = FormAuthenticationConstants.DEFAULT_CONTEXT_ID),
        @Property(name = FormAuthenticationConstants.PROP_RANKING,
                value = FormAuthenticationConstants.DEFAULT_RANKING),
        @Property(name = FormAuthenticationConstants.PROP_FORM_PARAM_NAME_USERNAME,
                value = FormAuthenticationConstants.DEFAULT_FORM_PARAM_NAME_USERNAME),
        @Property(name = FormAuthenticationConstants.PROP_FORM_PARAM_NAME_PASSWORD,
                value = FormAuthenticationConstants.DEFAULT_FORM_PARAM_NAME_PASSWORD),
        @Property(name = FormAuthenticationConstants.PROP_FORM_PARAM_NAME_SUCCESS_URL,
                value = FormAuthenticationConstants.DEFAULT_FORM_PARAM_NAME_SUCCESS_URL),
        @Property(name = FormAuthenticationConstants.PROP_FORM_PARAM_NAME_FAILED_URL,
                value = FormAuthenticationConstants.DEFAULT_FORM_PARAM_NAME_FAILED_URL),
        @Property(name = FormAuthenticationConstants.PROP_AUTHENTICATOR),
        @Property(name = FormAuthenticationConstants.PROP_RESOURCE_ID_RESOLVER),
        @Property(name = FormAuthenticationConstants.PROP_AUTHENTICATION_PROPAGATOR),
        @Property(name = FormAuthenticationConstants.PROP_LOG_SERVICE),
})
public class FormAuthenticationComponent {

    @Reference(bind = "setAuthenticator")
    private Authenticator authenticator;

    @Reference(bind = "setResourceIdResolver")
    private ResourceIdResolver resourceIdResolver;

    @Reference(bind = "setAuthenticationContext")
    private AuthenticationContext authenticationContext;

    @Reference(bind = "setAuthenticationPropagator")
    private AuthenticationPropagator authenticationPropagator;

    @Reference(bind = "setLogService")
    private LogService logService;

    private ServiceRegistration<Filter> authenticationFilterSR;

    private ServiceRegistration<Servlet> formAuthenticationServletSR;

    @Activate
    public void activate(final BundleContext context, final Map<String, Object> componentProperties) throws Exception {
        Filter authenticationFilter = new AuthenticationFilter(authenticationPropagator, authenticationContext,
                logService);

        String filterName =
                getStringProperty(componentProperties, FormAuthenticationConstants.PROP_FILTER_NAME);
        String pattern =
                getStringProperty(componentProperties, HttpWhiteboardConstants.PATTERN);
        String contextId =
                getStringProperty(componentProperties, HttpWhiteboardConstants.CONTEXT_ID);
        Long ranking =
                Long.valueOf(getStringProperty(componentProperties, FormAuthenticationConstants.PROP_RANKING));

        Dictionary<String, Object> filterProperties = new Hashtable<>();
        filterProperties.put(FormAuthenticationConstants.PROP_FILTER_NAME, filterName);
        filterProperties.put(HttpWhiteboardConstants.PATTERN, pattern);
        filterProperties.put(HttpWhiteboardConstants.CONTEXT_ID, contextId);
        filterProperties.put(Constants.SERVICE_RANKING, ranking);
        authenticationFilterSR =
                context.registerService(Filter.class, authenticationFilter, filterProperties);

        String formParamNameUsername = getStringProperty(componentProperties,
                FormAuthenticationConstants.PROP_FORM_PARAM_NAME_USERNAME);
        String formParamNamePassword = getStringProperty(componentProperties,
                FormAuthenticationConstants.PROP_FORM_PARAM_NAME_PASSWORD);
        String formParamNameSuccessUrl = getStringProperty(componentProperties,
                FormAuthenticationConstants.PROP_FORM_PARAM_NAME_SUCCESS_URL);
        String formParamNameFailedUrl = getStringProperty(componentProperties,
                FormAuthenticationConstants.PROP_FORM_PARAM_NAME_FAILED_URL);
        String servletAlias = getStringProperty(componentProperties,
                HttpWhiteboardConstants.ALIAS);

        Servlet formAuthenticationServlet = new FormAuthenticationServlet(authenticator, resourceIdResolver,
                authenticationPropagator, logService, formParamNameUsername, formParamNamePassword,
                formParamNameSuccessUrl, formParamNameFailedUrl);

        Dictionary<String, Object> servletProperties = new Hashtable<>();
        servletProperties.put(HttpWhiteboardConstants.ALIAS, servletAlias);
        servletProperties.put(HttpWhiteboardConstants.CONTEXT_ID, contextId);
        formAuthenticationServletSR =
                context.registerService(Servlet.class, formAuthenticationServlet, servletProperties);
    }

    @Deactivate
    public void deactivate() {
        if (authenticationFilterSR != null) {
            authenticationFilterSR.unregister();
            authenticationFilterSR = null;
        }
        if (formAuthenticationServletSR != null) {
            formAuthenticationServletSR.unregister();
            formAuthenticationServletSR = null;
        }
    }

    private String getStringProperty(final Map<String, Object> componentProperties, final String propertyName)
            throws ConfigurationException {
        Object value = componentProperties.get(propertyName);
        if (value == null) {
            throw new ConfigurationException(propertyName, "property not defined");
        }
        return String.valueOf(value);
    }

    public void setAuthenticationContext(final AuthenticationContext authenticationContext) {
        this.authenticationContext = authenticationContext;
    }

    public void setAuthenticationPropagator(final AuthenticationPropagator authenticationPropagator) {
        this.authenticationPropagator = authenticationPropagator;
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
