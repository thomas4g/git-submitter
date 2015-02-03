Submission Tool
============

What It Does
------------
Zips student code, authenticates them against the GT github api, creates a repo under their name, and adds us as collaborators

Building
--------
1. `ant jar`
2. To specify an alternative config file, `ant jar -Dconfig.file=someOtherConfig.json`

Running
-------
`java -jar dist/C3AS.jar MyJavaFile.java AllThe.java Such.java So.java Wow.java`

Packaging for an Assignment
---------------------------
`./package.sh zip-file-prefix config.json JUnitTest.class Code.java SomeMore.java Etc.java`

Files/Folders/What Is it All
----------------------------
`dist/` - contains the jars and a sample build file for the students use
`lib/` - external resources, for now just gson
`src/` - all da codes
