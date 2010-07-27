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
import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionResult;
import org.apache.maven.model.Build;
import org.apache.maven.project.MavenProject;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.PlexusContainerException;
import org.codehaus.plexus.component.repository.exception.ComponentLookupException;
import org.codehaus.plexus.logging.console.ConsoleLoggerManager;
import org.jboss.shrinkwrap.api.Archive;
import org.jboss.shrinkwrap.api.Assignable;
import org.jboss.shrinkwrap.api.importer.ZipImporter;
import org.jboss.shrinkwrap.impl.base.AssignableBase;
import org.jboss.shrinkwrap.maven.api.MavenImporter;

/**
 * Implementation of a {@link MavenImporter}, a ShrinkWrap
 * {@link Assignable} type capable of creating an {@link Archive}
 * from a Maven POM descriptor.
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 */
//TODO The impl currently does no artifact resolution in a repo, instead invoking the package phase
// for the project with the specified POM
public class MavenImporterImpl extends AssignableBase implements MavenImporter
{
   //-------------------------------------------------------------------------------------||
   // Class Members ----------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Logger
    */
   private static final Logger log = Logger.getLogger(MavenImporterImpl.class.getName());

   /**
    * Our hook to Maven Embedder
    */
   private static final DefaultPlexusContainer container;
   static
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

   /**
    * Name of the Maven "package" goal
    */
   private static final String NAME_GOAL_PACKAGE = "package";

   /**
    * Name of the property denoting whether or not to skip tests during the package phase
    */
   private static final String PROP_SKIP_TESTS = "maven.test.skip";
   
   /**
    * '.'
    */
   private static final char DELIMETER_DOT = '.';

   //-------------------------------------------------------------------------------------||
   // Instance Members -------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Underlying archive instance
    */
   private final Archive<?> archive;

   //-------------------------------------------------------------------------------------||
   // Constructor ------------------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * Creates a new instance using the specified underlying 
    * {@link Archive}.  The local repository used to resolve artifacts 
    * 
    * 
    * @param archive
    * @throws IllegalArgumentException If the archive is not specified
    */
   public MavenImporterImpl(final Archive<?> archive) throws IllegalArgumentException
   {
      // Precondition checks
      if (archive == null)
      {
         throw new IllegalArgumentException("Archive must be specified");
      }

      // Set
      this.archive = archive;
   }

   //-------------------------------------------------------------------------------------||
   // Required Implementations -----------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   /**
    * {@inheritDoc}
    * @see org.jboss.shrinkwrap.impl.base.AssignableBase#getArchive()
    */
   @Override
   protected Archive<?> getArchive()
   {
      return archive;
   }

   /**
    * {@inheritDoc}
    * @see org.jboss.shrinkwrap.maven.api.MavenImporter#fromPom(java.io.File)
    */
   @Override
   public MavenImporter fromPom(final File pom)
   {
      importArtifact(generateArtifact(pom));
      return this;
   }

   //-------------------------------------------------------------------------------------||
   // Internal Helper Members ------------------------------------------------------------||
   //-------------------------------------------------------------------------------------||

   //TODO Assumes ZIP format
   private void importArtifact(final File artifact)
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

   /**
    * Generates an artifact by invoking the "package"
    * goal upon the project with the specified POM
    * @param pom
    * @return
    * @throws IllegalArgumentException If the specified File is not a valid POM
    */
   private File generateArtifact(final File pom) throws IllegalArgumentException
   {
      // Precondition checks
      if (pom == null)
      {
         throw new IllegalArgumentException("POM must be specified");
      }
      if (!pom.exists())
      {
         throw new IllegalArgumentException("Specified POM does not exist " + pom.getAbsolutePath());
      }
      if (pom.isDirectory())
      {
         throw new IllegalArgumentException("POM must point to a valid file, not a directory: " + pom.getAbsolutePath());
      }

      //TODO Why are we capturing this? We never set TCCL to anything else.
      final ClassLoader previousTCCL = Thread.currentThread().getContextClassLoader();

      //TODO Don't need any artifact layout if we're invoking a build
      //      ArtifactRepositoryLayout layout;
      //      try
      //      {
      //         layout = container.lookup(ArtifactRepositoryLayout.class);
      //      }
      //      catch (ComponentLookupException e)
      //      {
      //         throw new RuntimeException("Could not lookup " + ArtifactRepositoryLayout.class, e);
      //      }

      // Get a hook to the Maven Embedder via the Container
      final Maven maven;
      try
      {
         maven = container.lookup(Maven.class);
      }
      catch (final ComponentLookupException e)
      {
         throw new RuntimeException("Could not lookup " + Maven.class, e);
      }

      // Construct a new request for Maven to package
      final MavenExecutionRequest request = new DefaultMavenExecutionRequest();
      //TODO We don't need a repository if we're just invoking the package phase on our own
      //      request.setLocalRepository(new MavenArtifactRepository("local", "file://" + System.getProperty("user.home")
      //            + "/.m2/repository", layout, new ArtifactRepositoryPolicy(true,
      //            ArtifactRepositoryPolicy.UPDATE_POLICY_NEVER, ArtifactRepositoryPolicy.CHECKSUM_POLICY_WARN),
      //            new ArtifactRepositoryPolicy(true, ArtifactRepositoryPolicy.UPDATE_POLICY_NEVER,
      //                  ArtifactRepositoryPolicy.CHECKSUM_POLICY_WARN)));

      request.setPom(pom);
      request.setBaseDirectory(pom.getParentFile());
      request.setGoals(Arrays.asList(NAME_GOAL_PACKAGE));
      request.setInteractiveMode(false);
      request.setOffline(true);
      request.setNoSnapshotUpdates(true);

      // Define user props to skip testing
      Properties userProps = new Properties();
      userProps.setProperty(PROP_SKIP_TESTS, Boolean.TRUE.toString());
      request.setUserProperties(userProps);

      // Execute the request
      final MavenExecutionResult result = maven.execute(request);

      // Determine if we ran into errors and rethrow accordingly
      if (result.hasExceptions())
      {
         if (result.getExceptions().size() == 1)
         {
            throw new RuntimeException("Exception thrown during build", result.getExceptions().get(0));
         }
         else
         {
            for (final Throwable exception : result.getExceptions())
            {
               log.log(Level.SEVERE, "Exception thrown during build", exception);
            }
            throw new RuntimeException("Multiple Exceptions during build, see log");
         }
      }

      // Get out the output of the passed result
      final MavenProject project = result.getProject();
      final Build build = project.getBuild();

      //TODO  Why?  We're not setting TCCL; is Maven? 
      Thread.currentThread().setContextClassLoader(previousTCCL);

      return new File(build.getDirectory(), build.getFinalName() + DELIMETER_DOT + project.getPackaging());
   }
}
