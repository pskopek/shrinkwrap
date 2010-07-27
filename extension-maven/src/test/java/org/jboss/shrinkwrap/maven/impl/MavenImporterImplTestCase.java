/*
 * JBoss, Home of Professional Open Source
 * Copyright 2010, Red Hat Middleware LLC, and individual contributors
 * by the @authors tag. See the copyright.txt in the distribution for a
 * full listing of individual contributors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * http://www.apache.org/licenses/LICENSE-2.0
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.jboss.shrinkwrap.maven.impl;

import java.io.File;
import java.util.logging.Logger;

import org.jboss.shrinkwrap.api.ArchivePath;
import org.jboss.shrinkwrap.api.ArchivePaths;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.formatter.Formatters;
import org.jboss.shrinkwrap.api.spec.JavaArchive;
import org.jboss.shrinkwrap.maven.api.MavenImporter;
import org.junit.Assert;
import org.junit.Test;

/**
 * MavenImporterImplTestCase
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class MavenImporterImplTestCase
{

   private static Logger log = Logger.getLogger(MavenImporterImplTestCase.class.getName());
   
   @Test
   public void shouldBeAbleToImplementAMavenGeneratedArtifact() throws Exception
   {
     
      long start = System.currentTimeMillis();
      JavaArchive jar = ShrinkWrap.create(JavaArchive.class)
                                    .as(MavenImporter.class)
                                       .fromPom(new File("src/test/resources/test-project/pom.xml"))
                                    .as(JavaArchive.class);
     
      log.info(jar.toString(Formatters.VERBOSE));
      log.fine("build took: " + (System.currentTimeMillis() - start) + " ms");

      ArchivePath testPath = ArchivePaths.create("org/jboss/shrinkwrap/maven/DummyClassForTesting.class");
      Assert.assertTrue("Archive should contain class", jar.contains(testPath));
   }
}
