package org.jboss.shrinkwrap.maven;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;

public class MavenAPITestCase
{
   
   @Test
   public void addLibrary() throws Exception 
   {
      
//      ShrinkWrap.create(WebArchive.class)
//         .as(MavenImporter.class)
//            .addLibrary("org.test", "junit", "4.3", "test")
//            .importProject("pom.xml", MavenImporter.Scope.TEST);
      
      
      /*
       * Import 'current' project based on pom.xml:
       * 
       * E.G.
       * 
       * @CurrentProject
       * Archive<?> archive;
       * 
       * WebArchive war = MavenImporter.importProject(
       *    WebArchive.class,
       *    "pom.xml",
       *    Scope.TEST);
       *
       * 
       * Jar project
       *    - import target class folders
       *    X can't import libraries, JavaArchive is not a Library container
       *    
       *    Might as well just do ExplodedImport.import("target/classes").as(JavaArchive.class) manually
       *        - (we could parse pom to auto find target folders, but....)
       *    
       * War project
       *    - import target class folders
       *    - import libraries from given pom in given scope 
       *        - (if based on Test scope we need to exclude artifacts like arquillian/shrinkwrap and any other extension. 
       *            These could be placed in provided scope as well.. )
       *  
       * Ear project
       *    X we don't know how to build the Enterprise archive structure unless we duplicate the Maven ear plugin. 
       *        e.g. what is a Jar? Library of Module
       *    
       * In all cases we need to duplicate a lot of the functionality of the jar/war/rar/ear packager plugins to make it predictable 
       * in the fashion Maven does it. 
       * Or we can execute the packager plugins directly via Maven for you, but in that case you might as well create a separate test project 
       * for the Artifact and just import it there(see next).
       * 
       *     
       * Maven based artifact import:
       * 
       *    - Need to move test case execution to integration-test lifecycle so that the package is build before tests run.
       *        - Or have a separate Test project for the Artifact.
       *    
       *    At this point, the Package is all ready created and known, so no special Maven integration is needed. 
       *    You can do just do ZipImporter.import("target/mywar.war").as(WebArchive.class);
       *        - (we could parse pom to auto find finalName/version etc)
       *    
       * 
       * Maven Library import:
       * 
       * E.G.
       * 
       * ShrinkWrap.create(WebArchive.class)
       *    .addLibraries(Maven.resolve("group:artifact:classifier"));
       * 
       *    - We can 
       * 
       */
//      WebArchive war = MavenImporter.importProject(
//            WebArchive.class, 
//            "pom.xml", 
//            Scope.TEST);
   }
}
