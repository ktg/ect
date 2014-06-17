author: Alastair Hampshire

This is not actually a component. Not even close! Basically to create a media centre component you have 2 options:
1) write an add in for the media centre (http://msdn.microsoft.com/library/default.asp?url=/library/en-us/MedctrSDK/htm/mediacenteraddins.asp) and then write a component which talks to the addin, perhaps over TCP or whatever. Addins are written in C# and can control pretty much everything on the media centre.

2) use the Click To Record api (http://msdn.microsoft.com/library/default.asp?url=/library/en-us/medctrsdk/htm/clicktorecordfeature.asp). This allows you to write a c# program which schedules a recording. Can't do much else though. To do this you'll need to be able to call c# from java and there's some notes on doing that below.

Issues with calling C# from Java
================================

Note: the c# module must be on a local disk.

To compile the C# code into a .netmodule:
csc /t:module /R:ehrecobj.dll CSharpJniTest.cs

To compile the managed and unmanaged c++ code:
cl -IC:\j2sdk1.4.2_08\include -IC:\j2sdk1.4.2_08\include\win32 /clr /LD JniTestImpl.cpp -FeJniTestImpl.dll

where
	/clr tell it to use the common language runtime
	/LD compile as dll
	-Fe<name> output name	

Compile the java class with:
javac JniTest.java

run with:
java JniTest