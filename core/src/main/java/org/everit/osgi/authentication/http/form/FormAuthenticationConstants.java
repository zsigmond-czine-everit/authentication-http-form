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
package org.everit.osgi.authentication.http.form;

/**
 * Constants of the Form Authentication component.
 */
public final class FormAuthenticationConstants {

    /**
     * The service factory PID of the Form Authentication component.
     */
    public static final String SERVICE_FACTORYPID_FORM_AUTHENTICATION =
            "org.everit.osgi.authentication.http.form.FormAuthentication";

    public static final String PROP_FILTER_NAME = "filterName";

    public static final String PROP_RANKING = "ranking";

    public static final String PROP_AUTHENTICATOR = "authenticator.target";

    public static final String PROP_RESOURCE_ID_RESOLVER = "resourceIdResolver.target";

    public static final String PROP_AUTHENTICATION_PROPAGATOR = "authenticationPropagator.target";

    public static final String PROP_LOG_SERVICE = "logService.target";

    public static final String PROP_FORM_PARAM_NAME_USERNAME = "form.param.name.username";

    public static final String PROP_FORM_PARAM_NAME_PASSWORD = "form.param.name.password";

    public static final String PROP_FORM_PARAM_NAME_SUCCESS_URL = "form.param.name.success.url";

    public static final String PROP_FORM_PARAM_NAME_FAILED_URL = "form.param.name.failed.url";

    /**
     * The default value of the {@link #PROP_FILTER_NAME}.
     */
    public static final String DEFAULT_FILTER_NAME = "AuthenticationFilter";

    /**
     * The default value of the {@link org.apache.felix.http.whiteboard.HttpWhiteboardConstants#PATTERN}.
     */
    public static final String DEFAULT_PATTERN = "/.*";

    /**
     * The default value of the {@link org.apache.felix.http.whiteboard.HttpWhiteboardConstants#ALIAS}.
     */
    public static final String DEFAULT_ALIAS = "/login-action";

    /**
     * The default value of the {@link org.apache.felix.http.whiteboard.HttpWhiteboardConstants#CONTEXT_ID}.
     */
    public static final String DEFAULT_CONTEXT_ID = "defaultContext";

    /**
     * The default value of the {@link #PROP_RANKING}.
     */
    public static final String DEFAULT_RANKING = "0";

    public static final String DEFAULT_FORM_PARAM_NAME_USERNAME = "username";

    public static final String DEFAULT_FORM_PARAM_NAME_PASSWORD = "password";

    public static final String DEFAULT_FORM_PARAM_NAME_SUCCESS_URL = "successUrl";

    public static final String DEFAULT_FORM_PARAM_NAME_FAILED_URL = "failedUrl";

    private FormAuthenticationConstants() {
    }

}
