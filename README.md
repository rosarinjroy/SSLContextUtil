SSL Context Util - Introduction
===============================
This utility helps you in easily adding ability to perform certificate based client authentication. This utility can be easily integrated with frameworks like HttpClient, Axis, Metro, Jersey. You can also use this tool to replace the default SSLContext in JDK.

Never use the system properties javax.net.ssl.*
===============================================
The main problem with using the system properties to specify which key store/trust store to use is that you cannot gurantee if your key store was the first one to be loaded. The behavior of the JSSE is that it reads and loads the key store specified in the system property only once.

If your program is a stand alone program where you set these properties at the beginning of the main() mehtod, then you are fine. But if you are developing a web application which is deployed in a container like Tomcat or Jetty, chances are that the key store might have been accessed at least once before even the control comes to your program. So essentially the System.setProperty() will be a no-op. You can somewhat address this problem by specifying the key store using "-D" flags to your JVM.

The seond problem that you cannot solve using the system properties is that you might have to use more than one key stores. For clarity purposes, I divide the keys and associated certificates into different key stores with different passwords. This results in a cleaner organization as well as better troubleshooting.

Enough talk, how it works
=========================
FIXME

Caveats
=======
While aimed at being simple and easy to integrate, please understand that the following caveats:
- The verification of server's CN is not enforced. So if your DNS is poisoned, you might be talking to a wrong server. But everytime when the utility connects to a server, it clearly prints the CN of the server. Hence you should be able to point out from the logs if at all the client connected to the wrong server. (I am working on this to add full support for enforcing the server name match)

