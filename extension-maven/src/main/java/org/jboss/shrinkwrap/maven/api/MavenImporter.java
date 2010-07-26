package org.jboss.shrinkwrap.maven.api;

import java.io.File;

import org.jboss.shrinkwrap.api.Assignable;

public interface MavenImporter extends Assignable
{ 

   MavenImporter from(File pom);
   
}
