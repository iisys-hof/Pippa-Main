# Pippa Main

## Prerequisites

### Voice Recording

The system currently only makes use of the "default" input device. This has to be set within the operating systems settings.

### Voice Recognition

The system uses a java-process-builder sub-process to invocate speech recognition. 
The choice of recognizer engine is currently hardcoded in SpeechRecognizer.java.
Also hardcoded is the command that is used to start speech recognition (in DeepSpeechSTT.java, respectively JuliusSTT.java).
These commands have to be adjusted to the properties of your system and speech-recognizer-installation.

#### DeepSpeech 
https://github.com/mozilla/DeepSpeech  
Contains instructions on how to use a virtual environment for pip to install DeepSpeech and how to obtain and use the pre-trained model.

#### Julius
https://github.com/julius-speech/julius  
Contains instructions for the general usage of Julius and where to obtain a pre-trained model.

https://julius.osdn.jp/juliusbook/en/desc_install.html  
Contains detailed installation instructions for Julius. Make sure to set the '--enable-words-int ' 
option when compiling from source if you want to use the pre-trained  large vocabulary model.

### Voice Out

The system currently uses Flite (http://www.festvox.org/flite/) to create a synthetic speech output. 
Flite can be installed through the package sources on most linux distributions. Similar to the speech recognition,
a sub-process is used to invocate speech-synthetisation. The command for invocation is hardcoded into FliteTTS.java and might work out of the box.

### Runtime

The application is currently dependent on the use of Oracle JDK/JRE 8. Using OpenJDK will lead to errors.  
https://wiki.ubuntuusers.de/Java/Installation/Oracle_Java/Java_8/

## Installation

### Working folders

Currently, the application itself can be started from any location since all working paths are hardcoded. 
Though the application will try to produce the needed structure it is advisable to pre-create it and make sure
that the application has read/write-access to these folders. The 'Bundles' directory will never be created automatically and has to be pre-created in it's entirety.

<pre>
 Desktop  
    ├── Bundles                 
    │       ├── Core            
    │       │       └── plugins
    │       ├── Service         
    │       │       └── plugins 
    │       ├── Skill           
    │       │       └── plugins 
    │       ├── Support  
    │       │       └── plugins 
    │       └── System  
    │               └── plugins 
    ├── Log  
    ├── MainConfig  
    ├── SkillConfig  
    └── Sound  
</pre>

### Cloning the repository, creating the bundles and running the application

Clone the repository. Make changes in the code where needed. Export all packages that contain an OSGi MANIFEST.MF as OSGi-bundles.
Those are currently all packages except 'de.iisys.pippa.main'. Then sort the created bundles into the folder structure that is described above.
The part of the package name after 'de.iisys.pippa' describes where each bundle belongs. 

Example: The 'de.iisys.pippa.**service**.speech_out' package will produce a bundle called 'de.iisys.pippa.**service**.speech_out_x.x.x.jar'.  
This bundle has to go into Desktop/Bundles/**Service**/plugins/

Finally, compile and run de.iisys.pippa.main with the main class Main.java.
This will start the OSGi-framework and load all bundles from the created folder structure.
Make sure to only have a single version of each bundle since the framework does not do any version reconciliation and the choice of bundle might be random.


### Summary

- set the default input device to the microphone of your choice
- install a speech recognition engine
- install Flite
- install Oracle Java as your runtime
- create folder structure
- clone the repository
- make changes in the code where needed (speech recognition, speech synthesis)
- export all packages as bundles except 'de.iisys.pippa.main'
- compile and run 'de.issys.pippa.main'

## Legacy

Thanks to group music-player the Core package can now be included into your projects through your local Maven.

To do so, cd into the package's project folder where the new pom.xml is located and execute `mvn install`.
This will install the package into your local Maven-repository. 
Afterwards, the following dependency let's you include the artifact into your projects. 
Be aware that you still need to set the proper version number, which is declared in both pom.xml and the bundle's manifest file.

`<dependency>    
    <groupId>de.iisys.pippa</groupId>
    <artifactId>core</artifactId>
    <version>X.X.X</version>
    <scope>provided</scope>
</dependency>`

Time permitting, there will be a complete transition to maven. Until then this will have to do.