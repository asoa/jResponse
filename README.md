# Summary
This Java application connects to a Microsoft SQL Server instance and provides a graphical user interface (Java FX) 
that provides the analyst with an interface to interrogate computer systems. The interrogation is done using PowerShell queries using 
Microsoft’s implementation of the Common Information Model (CIM) (“Common Information Model,” n.d.). The application sends the PowerShell 
queries to selected computers in parallel using Java’s native concurrency library and then binds the returned results to elements of the 
Java FX GUI to present to the user. The application then writes the returned results to the database for storage using 
Microsoft’s Java Database Connectivity Driver (JDBC). 

# Features
The application has three major features: discovery, enumeration, and analysis. These features are presented as tabs within the GUI.  The flow of the application begins with a ping scan that is executed on the discovery tab to identify active computer systems on the network.  The results of this scan are propagated to the enumeration tab—and written to the database—where the user can choose from multiple buttons to investigate particular features of the computer(s)—i.e. running processes, operating system information, logged on user, etc. The results of these queries are returned to the GUI using the binding process discussed earlier, and are also written to the database. Currently, the application only supports retrieving system information from Windows computers. SOC analysts must conduct many of their tasks in parallel as many of them are time sensitive. The application’s design accounted for this constraint by extensively using Java’s concurrency library so that all major functions of the database can be executed in parallel.

