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
import java.util.Arrays;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipFile;

import org.apache.maven.Maven;
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
import org.apache.maven.artifact.repository.MavenArtifactRepository;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionResult;
import org.apache.maven.model.Build;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.logging.console.ConsoleLoggerManager;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.impl.base.AssignableBase;
import org.jboss.shrinkwrap.maven.api.MavenImporter;

/**
 * MavenImporterImpl
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class MavenImporterImpl extends AssignableBase implements MavenImporter
{
   private static final Logger log = Logger.getLogger(MavenImporterImpl.class.getName());
   
   private static DefaultPlexusContainer container = null; 
   
   private PlexusContainer getInstance() 
   {
      if(container == null)
      {
         try
         {
            container = new DefaultPlexusContainer();
            container.setLoggerManager(new ConsoleLoggerManager("ERROR"));
         } 
         catch (PlexusContainerException e) 
         {
            throw new RuntimeException("Could not initiate Maven Container", e);
         }
      }
      return container;
   }
   
   
   private Archive<?> archive;
   
   public MavenImporterImpl(Archive<?> archive)
   {
      if(archive == null)
      {
         throw new IllegalArgumentException("Archive must be specified");
      }
      this.archive = archive;
   }

   @Override
   protected Archive<?> getArchive()
   {
      return archive;
   }
   
   @Override
   public MavenImporter from(File pom)
   {
      importArtifact(generateArtifact(pom));
      return this;
   }
   
   private void importArtifact(File artifact)
   {
      try
      {
         archive.as(ZipImporter.class).importZip(new ZipFile(artifact));
      }
      catch (Exception e) 
      {
         throw new RuntimeException("Could not import artifact " + artifact.getAbsolutePath(), e);
      }
   }

   private File generateArtifact(File pom) 
   {
      
      ClassLoader previousTCCL = Thread.currentThread().getContextClassLoader();
      
      PlexusContainer container = getInstance();
      ArtifactRepositoryLayout layout;
      try
      {
         layout = container.lookup(ArtifactRepositoryLayout.class);
      }
      catch (ComponentLookupException e)
      {
         throw new RuntimeException("Could not lookup " + ArtifactRepositoryLayout.class, e);
      }
      Maven maven;
      try
      {
         maven = container.lookup(Maven.class);
      }
      catch (ComponentLookupException e)
      {
         throw new RuntimeException("Could not lookup " + Maven.class, e);
      }

      MavenExecutionRequest request = new DefaultMavenExecutionRequest();
      request.setLocalRepository(new MavenArtifactRepository(
            "local", 
            "file://" + System.getProperty("user.home") + "/.m2/repository", 
            layout, 
            new ArtifactRepositoryPolicy(true, ArtifactRepositoryPolicy.UPDATE_POLICY_NEVER, ArtifactRepositoryPolicy.CHECKSUM_POLICY_WARN), 
            new ArtifactRepositoryPolicy(true, ArtifactRepositoryPolicy.UPDATE_POLICY_NEVER, ArtifactRepositoryPolicy.CHECKSUM_POLICY_WARN)));

      request.setPom(pom);
      request.setBaseDirectory(pom.getParentFile());
      request.setGoals(Arrays.asList("package"));
      request.setInteractiveMode(false);
      request.setOffline(true);
      
      Properties userProps = new Properties();
      userProps.setProperty("maven.test.skip", "true");
      
      request.setUserProperties(userProps);
      
      
      MavenExecutionResult result = maven.execute(request);
      
      if(result.hasExceptions())
      {
         if(result.getExceptions().size() == 1)
         {
            throw new RuntimeException("Exception thrown during build", result.getExceptions().get(0));
         }
         else
         {
            for(Throwable exception : result.getExceptions())
            {
               log.log(Level.SEVERE, "Exception thrown during build", exception);
            }
            throw new RuntimeException("Multiple Exceptions during build, see log");
         }
      }
      
      MavenProject project = result.getProject();
      Build build = project.getBuild();
      
      Thread.currentThread().setContextClassLoader(previousTCCL);
      
      return new File(build.getDirectory(), build.getFinalName() + "." + project.getPackaging());
   }
}
