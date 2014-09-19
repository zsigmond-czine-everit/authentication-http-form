/**
 * This file is part of Everit - HTML form-based authentication tests.
 *
 * Everit - HTML form-based authentication tests is free software: you can redistribute it and/or modify
 * it under the terms of the GNU Lesser General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Everit - HTML form-based authentication tests is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public License
 * along with Everit - HTML form-based authentication tests.  If not, see <http://www.gnu.org/licenses/>.
 */
package org.everit.osgi.authentication.http.form.tests;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.servlet.Filter;
import javax.servlet.Servlet;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.io.IOUtils;
import org.apache.felix.scr.annotations.Activate;
import org.apache.felix.scr.annotations.Component;
import org.apache.felix.scr.annotations.ConfigurationPolicy;
import org.apache.felix.scr.annotations.Deactivate;
import org.apache.felix.scr.annotations.Properties;
import org.apache.felix.scr.annotations.Property;
import org.apache.felix.scr.annotations.Reference;
import org.apache.felix.scr.annotations.Service;
import org.apache.http.Header;
import org.apache.http.HttpEntity;
import org.apache.http.HttpResponse;
import org.apache.http.NameValuePair;
import org.apache.http.client.CookieStore;
import org.apache.http.client.HttpClient;
import org.apache.http.client.entity.UrlEncodedFormEntity;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.client.protocol.ClientContext;
import org.apache.http.impl.client.BasicCookieStore;
import org.apache.http.impl.client.DefaultHttpClient;
import org.apache.http.message.BasicNameValuePair;
import org.apache.http.protocol.BasicHttpContext;
import org.apache.http.protocol.HttpContext;
import org.eclipse.jetty.server.Server;
import org.eclipse.jetty.servlet.FilterHolder;
import org.eclipse.jetty.servlet.ServletContextHandler;
import org.eclipse.jetty.servlet.ServletHolder;
import org.everit.osgi.authentication.context.AuthenticationContext;
import org.everit.osgi.authentication.simple.SimpleSubject;
import org.everit.osgi.authentication.simple.SimpleSubjectManager;
import org.everit.osgi.dev.testrunner.TestRunnerConstants;
import org.everit.osgi.resource.ResourceService;
import org.junit.Assert;
import org.junit.Test;
import org.osgi.framework.BundleContext;

@Component(name = "FormAuthenticationServletTest", metatype = true, configurationFactory = false,
        policy = ConfigurationPolicy.REQUIRE, immediate = true)
@Properties({
        @Property(name = TestRunnerConstants.SERVICE_PROPERTY_TESTRUNNER_ENGINE_TYPE, value = "junit4"),
        @Property(name = TestRunnerConstants.SERVICE_PROPERTY_TEST_ID, value = "FormAuthenticationServletTest"),
        @Property(name = "simpleSubjectManager.target"),
        @Property(name = "resourceService.target"),
        @Property(name = "authenticationContext.target"),
        @Property(name = "helloWorldServlet.target"),
        @Property(name = "formAuthenticationServlet.target"),
        @Property(name = "sessionAuthenticationFilter.target")
})
@Service(value = FormAuthenticationServletTestComponent.class)
public class FormAuthenticationServletTestComponent {

    private static final String LOGIN_SUCCESS_ALIAS = "/login-success.html";

    private static final String LOGIN_FAILED_ALIAS = "/login-failed.html";

    private static final String LOGIN_ACTION = "/login-action";

    private static final String HELLO_SERVLET_ALIAS = "/hello";

    private static final String USERNAME = "Aladdin";

    private static final String PASSWORD = "open sesame";

    private static final String WRONG_PASSWORD = PASSWORD + PASSWORD;

    @Reference(bind = "setSimpleSubjectManager")
    private SimpleSubjectManager simpleSubjectManager;

    @Reference(bind = "setResourceService")
    private ResourceService resourceService;

    @Reference(bind = "setAuthenticationContext")
    private AuthenticationContext authenticationContext;

    @Reference(bind = "setHelloWorldServlet")
    private Servlet helloWorldServlet;

    @Reference(bind = "setBelloWorldServlet")
    private Servlet belloWorldServlet;

    @Reference(bind = "setFormAuthenticationServlet")
    private Servlet formAuthenticationServlet;

    @Reference(bind = "setSessionAuthenticationFilter")
    private Filter sessionAuthenticationFilter;

    private Server testServer;

    private String helloUrl;

    private String loginActionUrl;

    private String loginFailedUrl;

    private String loginSuccessUrl;

    private long authenticatedResourceId;

    private long defaultResourceId;

    @Activate
    public void activate(final BundleContext context, final Map<String, Object> componentProperties)
            throws Exception {
        testServer = new Server(0);
        ServletContextHandler servletContextHandler = new ServletContextHandler(ServletContextHandler.SESSIONS);
        testServer.setHandler(servletContextHandler);

        servletContextHandler.addFilter(
                new FilterHolder(sessionAuthenticationFilter), "/*", null);
        servletContextHandler.addServlet(
                new ServletHolder("helloWorldServlet", helloWorldServlet), HELLO_SERVLET_ALIAS);
        servletContextHandler.addServlet(
                new ServletHolder("formAuthenticationServlet", formAuthenticationServlet), LOGIN_ACTION);

        testServer.start();

        String testServerURI = testServer.getURI().toString();
        String testServerURL = testServerURI.substring(0, testServerURI.length() - 1);

        helloUrl = testServerURL + HELLO_SERVLET_ALIAS;
        loginActionUrl = testServerURL + LOGIN_ACTION;
        loginFailedUrl = testServerURL + LOGIN_FAILED_ALIAS;
        loginSuccessUrl = testServerURL + LOGIN_SUCCESS_ALIAS;

        long resourceId = resourceService.createResource();
        simpleSubjectManager.delete(USERNAME);
        SimpleSubject simpleSubject = simpleSubjectManager.create(resourceId, USERNAME, PASSWORD);
        authenticatedResourceId = simpleSubject.getResourceId();
        defaultResourceId = authenticationContext.getDefaultResourceId();
    }

    @Deactivate
    public void deactivate() throws Exception {
        if (testServer != null) {
            testServer.stop();
            testServer.destroy();
        }
    }

    private void hello(final HttpContext httpContext, final long expectedResourceId)
            throws IOException {
        HttpClient httpClient = new DefaultHttpClient();
        HttpGet httpGet = new HttpGet(helloUrl);
        HttpResponse httpResponse = httpClient.execute(httpGet, httpContext);
        Assert.assertEquals(HttpServletResponse.SC_OK, httpResponse.getStatusLine().getStatusCode());
        HttpEntity responseEntity = httpResponse.getEntity();
        InputStream inputStream = responseEntity.getContent();
        StringWriter writer = new StringWriter();
        IOUtils.copy(inputStream, writer);
        String responseBodyAsString = writer.toString();
        Assert.assertEquals(expectedResourceId, Long.valueOf(responseBodyAsString).longValue());
    }

    private void login(final HttpContext httpContext, final String username, final String password,
            final String expectedLocation)
            throws Exception {
        HttpClient httpClient = new DefaultHttpClient();
        HttpPost httpPost = new HttpPost(loginActionUrl);
        List<NameValuePair> parameters = new ArrayList<NameValuePair>();
        parameters.add(new BasicNameValuePair("username", username));
        parameters.add(new BasicNameValuePair("password", password));
        parameters.add(new BasicNameValuePair("successUrl", LOGIN_SUCCESS_ALIAS));
        parameters.add(new BasicNameValuePair("failedUrl", LOGIN_FAILED_ALIAS));
        HttpEntity entity = new UrlEncodedFormEntity(parameters);
        httpPost.setEntity(entity);
        HttpResponse httpResponse = httpClient.execute(httpPost, httpContext);
        Assert.assertEquals(HttpServletResponse.SC_MOVED_TEMPORARILY, httpResponse.getStatusLine().getStatusCode());
        Header locationHeader = httpResponse.getFirstHeader("Location");
        Assert.assertEquals(expectedLocation, locationHeader.getValue());
    }

    public void setAuthenticationContext(final AuthenticationContext authenticationContext) {
        this.authenticationContext = authenticationContext;
    }

    public void setBelloWorldServlet(final Servlet belloWorldServlet) {
        this.belloWorldServlet = belloWorldServlet;
    }

    public void setFormAuthenticationServlet(final Servlet formAuthenticationServlet) {
        this.formAuthenticationServlet = formAuthenticationServlet;
    }

    public void setHelloWorldServlet(final Servlet helloWorldServlet) {
        this.helloWorldServlet = helloWorldServlet;
    }

    public void setResourceService(final ResourceService resourceService) {
        this.resourceService = resourceService;
    }

    public void setSessionAuthenticationFilter(final Filter sessionAuthenticationFilter) {
        this.sessionAuthenticationFilter = sessionAuthenticationFilter;
    }

    public void setSimpleSubjectManager(final SimpleSubjectManager simpleSubjectManager) {
        this.simpleSubjectManager = simpleSubjectManager;
    }

    @Test
    public void testAccessHelloPage() throws Exception {
        CookieStore cookieStore = new BasicCookieStore();
        HttpContext httpContext = new BasicHttpContext();
        httpContext.setAttribute(ClientContext.COOKIE_STORE, cookieStore);

        hello(httpContext, defaultResourceId);
        login(httpContext, USERNAME, WRONG_PASSWORD, loginFailedUrl);
        login(httpContext, USERNAME, PASSWORD, loginSuccessUrl);
        hello(httpContext, authenticatedResourceId);
    }

}
