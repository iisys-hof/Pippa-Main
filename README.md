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