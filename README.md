# Rhidmo&reg;
Developing JavaScript-based extensions for [SAP&reg; Identity Management (IDM) 8.0](http://go.sap.com/product/technology-platform/identity-management.html)

## What is it?
Rhidmo&reg; is a generic implementation of the SAP&reg; Identity Management Extension Framework. It enables developers to build custom SAP&reg; IDM extensions in JavaScript instead of Java, directly from SAP&reg; Identity Management Developer Studio.

Rhidmo&reg; is free and open source software available under the [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0.txt). Commercial consulting, implementation services and support are available from Foxysoft GmbH in Germany. Visit http://foxysoft.de for more information.

## Build
You need [Git](https://git-scm.com/), Java6+ and [Maven 3.x](https://maven.apache.org/) to build. Maven downloads dependencies from the Internet by default, so your build machine will need to be connected to the Internet.

     git clone https://github.com/foxysoft/idm-extension-rhidmo
     cd idm-extension-rhidmo
     mvn package
     
## Deploy
The easiest way to deploy is to copy rhidmo-ear-&lt;VERSION&gt;.ear to your SAP&reg; NetWeaver host, and use a local telnet connection to deploy.

Assuming UNIX-like build environment (local) and SAP&reg; NetWeaver host (remote), use these shell commands to deploy directly from the project's root directory:

    scp ear/target/*.ear root@your-sap-jee-host:/tmp
    ssh root@your-sap-jee-host
    telnet localhost 50008
    deploy /tmp

Adjust host name and JEE telnet port according to your environment.
    
