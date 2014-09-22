authentication-http-form
========================

HTML form-based authentication Servlet.

#Component
The module contains one Declarative Services component. The component can be 
instantiated multiple times via Configuration Admin. The component registers 
a **javax.servlet.Servlet** OSGi service to handles form-based authentication 
requests. The following authentication mechanism is implemeted:
 - Checks the provided username and password parameters using the 
 [authenticator-api][3].
 - Maps the username to a Resource ID using the [resource-resolver-api][4].
 - Stores the mapped Resource ID in the HTTP Session. The name of the session 
 attribute is provided by the *AuthenticationSessionAttributeNames* interface 
 from the [authentication-http-session][5] module.

If any of the above steps fails, the user will be redirected to a failure URL, 
otherwise to a success URL.

##Important
This component is responsible to match the credentials and to store the 
authentication information in the session. It is recommended to use this 
component with [authentication-http-session][5] to read this authentication 
information from session and execute an authenticated process.

#Configuration
 - **Username parameter name**: The name of the form parameter that stores the 
 username.
 - **Password parameter name**: The name of the form parameter that stores the 
 password.
 - **Success URL**: The URL where the user will be redirected by default in 
 case of a successful authentication.
 - **Success URL parameter name**: The name of the parameter that stores the 
 URL where the user will be redirected in case of a successful authentication. 
 If the request contains this parameter, it overrides the "Success URL" 
 configuration. 
 - **Failed URL**: The URL where the user will be redirected by default in 
 case of a failed authentication.
 - **Failed URL parameter name**: The name of the parameter that stores the 
 URL where the user will be redirected in case of a failed authentication. 
 If the request contains this parameter, it overrides the "Failed URL" 
 configuration.

#Concept
Full authentication concept is available on blog post [Everit Authentication][1].
Implemented components based on this concept are listed [here][2].

[![Analytics](https://ga-beacon.appspot.com/UA-15041869-4/everit-org/authentication-http-form)](https://github.com/igrigorik/ga-beacon)


[1]: http://everitorg.wordpress.com/2014/07/31/everit-authentication/
[2]: http://everitorg.wordpress.com/2014/07/31/everit-authentication-implemented-and-released-2/
[3]: https://github.com/everit-org/authenticator-api
[4]: https://github.com/everit-org/resource-resolver-api
[5]: https://github.com/everit-org/authentication-http-session
