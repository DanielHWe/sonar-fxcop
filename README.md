# FxCop plugin for C# or VB.NET projects

[![Travis Build Status](https://travis-ci.org/DanielHWe/sonar-fxcop.svg?branch=master)](https://travis-ci.org/DanielHWe/sonar-fxcop)

[![Quality Gate](https://sonarcloud.io/api/badges/gate?key=org.sonarsource.dotnet:sonar-fxcop-plugin)](https://sonarcloud.io/dashboard?id=org.sonarsource.dotnet%3Asonar-fxcop-plugin)

### About

This is a plugin for Sonar Qube to scan C# and VB.net projects with the FxCop Tool. It is possible to scan a defined assemby by use of  "sonar.cs.fxcop.assembly" or to scan a solution by use of "sonar.cs.fxcop.slnFile".

The rules for FxCop can be activated in Sonar Qube Server Quality Profiles.

### Requirements

To use the FxCop plugin, you also need to 
* install C# plugin version &ge;5.4 and/or VB.NET plugin version &ge;3.0.
* set parameter sonar.cs.fxcop.assembly, sonar.cs.fxcop.project or sonar.cs.fxcop.sln

### Use with SonarQube Scanner for MSBuild v4.0

To use with **Version 4** and above you have to define "sonar.cs.fxcop.assembly", "sonar.cs.fxcop.fxCopCmdPath" and "sonar.cs.fxcop.directory" by **/d:"key:value"** for the **SonarQube.Scanner.MSBuild.exe** begin command.

**__Example with command line paramter:__**

SonarQube.Scanner.MSBuild.exe begin /n:"MyProject" /v:"0.9.0.99" /k:"MyProject_key" /d:"sonar.cs.fxcop.assembly=/bin/debug/My.dll" /d:"sonar.cs.fxcop.fxCopCmdPath=C:/Program Files (x86)/Microsoft Visual Studio 14.0/Team Tools/Static Analysis Tools/FxCop/FxCopCmd.exe" /d:"sonar.cs.fxcop.directory=bin\debug\"

**__Example with SonarQube.Analysis.xml:__**

Create a file called SonarQube.Analysis.xml, containing at minimum sonar.cs.fxcop.assembly, sonar.cs.fxcop.fxCopCmdPath and sonar.cs.fxcop.directory.

```xml
<?xml version="1.0" encoding="utf-8"?>
<SonarQubeAnalysisProperties xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns="http://www.sonarsource.com/msbuild/integration/2015/1">
    <Property Name="sonar.cs.fxcop.fxCopCmdPath">C:/Program Files (x86)/Microsoft Visual Studio 14.0/Team Tools/Static Analysis Tools/FxCop/FxCopCmd.exe</Property>
    <Property Name="sonar.cs.fxcop.directory">\bin\debug\</Property>
    <Property Name="sonar.cs.fxcop.assembly">\bin\debug\My.dll</Property>
</SonarQubeAnalysisProperties>
```

use /s to set the addditional xml file 

SonarQube.Scanner.MSBuild.exe begin /n:"MyProject" /v:"0.9.0.99" /k:"MyProject_key" /s:"C:\projects\MyDll\SonarQube.Analysis.xml"

### Scan on base of an sln file

With 1.3 RC 1 there is the possibility to scan a compleate sln file. (The plugin will generate a fxcop project file including all assemblies from steh solution to scan the assemblies).

Parameter is 'sonar.cs.fxcop.slnFile' while not use 'sonar.cs.fxcop.assembly'.

The plugin in version 1.3 will scan all projects in the solution include those that are excluded from scan or build.

**__Example:__**

SonarQube.Scanner.MSBuild.exe begin /n:"MyProject" /v:"0.9.0.99" /k:"MyProject_key" /d:"sonar.cs.fxcop.slnFile=MyProject.sln" /d:"sonar.cs.fxcop.fxCopCmdPath=C:/Program Files (x86)/Microsoft Visual Studio 14.0/Team Tools/Static Analysis Tools/FxCop/FxCopCmd.exe" /d:"sonar.cs.fxcop.directory=bin\debug\"

msbuild /t:Rebuild MyProject.sln

SonarQube.Scanner.MSBuild.exe end

**__Example with SonarQube.Analysis.xml:__**

Create a file called SonarQube.Analysis.xml, containing at minimum sonar.cs.fxcop.slnFile, sonar.cs.fxcop.fxCopCmdPath and sonar.cs.fxcop.directory.

```xml
<?xml version="1.0" encoding="utf-8"?>
<SonarQubeAnalysisProperties xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance" xmlns:xsd="http://www.w3.org/2001/XMLSchema" xmlns="http://www.sonarsource.com/msbuild/integration/2015/1">
    <Property Name="sonar.cs.fxcop.fxCopCmdPath">C:/Program Files (x86)/Microsoft Visual Studio 14.0/Team Tools/Static Analysis Tools/FxCop/FxCopCmd.exe</Property>
    <Property Name="sonar.cs.fxcop.directory">\bin\debug\</Property>
    <Property Name="sonar.cs.fxcop.slnFile">\bin\debug\MyProject.sln</Property>
</SonarQubeAnalysisProperties>
```

use /s to set the addditional xml file 

SonarQube.Scanner.MSBuild.exe begin /n:"MyProject" /v:"0.9.0.99" /k:"MyProject_key" /s:"C:\projects\MyDll\SonarQube.Analysis.xml"

msbuild /t:Rebuild MyProject.sln

SonarQube.Scanner.MSBuild.exe end
