SSL Context Util - Introduction
===============================
This utility helps you in easily adding ability to perform certificate based client authentication. This utility can be easily integrated with frameworks like HttpClient, Axis, Metro, Jersey. You can also use this tool to replace the default SSLContext in JDK.

Never use the system properties javax.net.ssl.*
===============================================
FIXME

Caveats
=======
While aimed at being simple and easy to integrate, please understand that the following caveats:
- The verification of server's CN is not enforced. So if your DNS is poisoned, you might be talking to a wrong server. But everytime when the utility connects to a server, it clearly prints the CN of the server. Hence you should be able to point out from the logs if at all the client connected to the wrong server. (I am working on this to add full support for enforcing the server name match)

