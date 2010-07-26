package org.jboss.shrinkwrap.maven.impl;

import java.io.File;

import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.formatter.Formatters;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.maven.api.MavenImporter;
import org.junit.Test;

public class MavenImporterImplTestCase
{

   @Test
   public void test() throws Exception 
   {
      for(int i = 0; i < 5; i++)
      {
         shouldBeAbleToImplementAMavenGeneratedArtifact();
      }
   }
   public void shouldBeAbleToImplementAMavenGeneratedArtifact() throws Exception
   {
      long start = System.currentTimeMillis();
      JavaArchive jar = ShrinkWrap.create(JavaArchive.class)
                                    .as(MavenImporter.class)
                                       .from(new File("/home/aslak/dev/source/testing/arquillian/junit/pom.xml"))
                                    .as(JavaArchive.class);
     
      System.out.println(jar.toString(Formatters.VERBOSE));
      
      System.out.println("build took: " + (System.currentTimeMillis() - start) + " ms");
   }
}
