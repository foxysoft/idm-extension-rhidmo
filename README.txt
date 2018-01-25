Rhidmo®

Developing JavaScript-based extensions for SAP® Identity Management (IDM) 8.0

What is it?

Rhidmo® is a generic implementation of the SAP® Identity Management Extension
Framework. It enables developers to build custom SAP® IDM extensions in
JavaScript instead of Java, directly from SAP® Identity Management Developer
Studio.

Rhidmo® is free and open source software available under the Apache License,
Version 2.0. Commercial consulting, implementation services and support are
available from Foxysoft GmbH in Germany. Visit http://foxysoft.de for more
information.

Build

You need Git, Java6+ and Maven 3.x to build. Maven downloads dependencies from
the Internet by default, so your build machine will need to be connected to the
Internet.

 git clone https://github.com/foxysoft/idm-extension-rhidmo
 cd idm-extension-rhidmo
 mvn package

Deploy

For a detailed installation and configuration guide, please refer to docs/
InstallationManual.pdf contained in this distribution. Here's a condensed
summary:

Copy rhidmo-ear-<VERSION>.ear to your SAP® NetWeaver host, and use a local
telnet connection to deploy.

Assuming UNIX-like build environment (local) and SAP® NetWeaver host (remote),
use these shell commands to deploy directly from the project's root directory:

scp ear/target/*.ear root@your-sap-jee-host:/tmp
ssh root@your-sap-jee-host
telnet localhost 50008
deploy /tmp

Adjust host name and JEE telnet port according to your environment.

