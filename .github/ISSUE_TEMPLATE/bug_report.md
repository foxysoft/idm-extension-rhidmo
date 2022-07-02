---
name: Bug report
about: Create a report to help us improve
title: ''
labels: bug
assignees: ''

---

**Symptom**
A clear and concise description of what you did and how actual behavior differs from expected behavior

**How to reproduce**
Include at least the following information:
1. Full JavaScript source of your Rhidmo script(s)
2. Screenshots of at least the "General" tab of Rhidmo form in Eclipse/SAP Identity Management Developer Studio. Provide a screenshot showing at least "Extension Class" and all "Custom Parameters".
3. Step-by-step instructions on how you use the form at runtime to get the error

**Logs**
Go to **SAP NetWeaver Administrator (NWA) > Troubleshooting > Logs and Traces > Log Viewer**. Inside Log Viewer, navigate to View > Open View... > Developer Traces. 

If there's any message with severity error and location de.foxysoft.\*, expand its content by clicking on the **Details** button (the + icon in the leftmost column). In the Log Record Details control, click the **Show Full Message** link. If an exception occurred, the full Java stack trace will be displayed, which usually contains the most valuable information we need to analyze and solve your problem.

Copy and paste the complete log message content into this GitHub issue.

**Version Information**
Provide the exact versions of the following software components:
- Rhidmo (e.g. 1.1.0) 
- SAP Identity Management (e.g. 8.0.5.0-SQL-2017-06-28)
- SAP Identity Management database type (e.g. Sybase)
- SAP NetWeaver AS Java (e.g. 7.50)
- SAP NetWeaver VM Java version (e.g. 1.8.0_51)

**Additional context**
Add any other context about the problem here.
