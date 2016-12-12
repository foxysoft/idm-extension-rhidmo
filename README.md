# idm-extfwk-poc
A proof of concept implementation of the [SAP&reg; Identity Management (IDM)](http://go.sap.com/product/technology-platform/identity-management.html) 8.0 Extension Framework
## What is it?
This is an add-on for SAP&reg; Identity Management (IDM) that allows developers to implement SAP&reg; IDM Extension Framework's onLoad and onSubmit methods using JavaScript instead of Java. The add-on is free and open source software available under the [Apache License, Version 2.0](https://www.apache.org/licenses/LICENSE-2.0.txt).
## Build
You need [Git](https://git-scm.com/), Java6+ and [Maven 3.x](https://maven.apache.org/) to build. Maven downloads dependencies from the Internet by default, so your build machine will need to be connected to the Internet.

     git clone https://github.com/boskamp/idm-extfwk-poc
     cd idm-extfwk-poc
     mvn package
     
## Deploy
The easiest way to deploy is to copy rhidmo-ear-&lt;VERSION&gt;.ear to your SAP&reg; JEE host, and use a local telnet connection to deploy.

Assuming SAP&reg; AS Java on Linux, this would work like:

    scp ear/target/*.ear root@your-sap-jee-host:/tmp
    ssh root@your-sap-jee-host
    telnet localhost 50008
    deploy /tmp

Adjust host name and JEE telnet port according to your environment.
    
