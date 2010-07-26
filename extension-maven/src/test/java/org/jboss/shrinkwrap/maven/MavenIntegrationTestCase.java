/*
 * JBoss, Home of Professional Open Source
 * Copyright 2009, Red Hat Middleware LLC, and individual contributors
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
package org.jboss.shrinkwrap.maven;

import java.io.File;
import java.util.Arrays;
import java.util.Set;

import org.apache.maven.Maven;
import org.apache.maven.artifact.Artifact;
import org.apache.maven.artifact.repository.ArtifactRepositoryPolicy;
import org.apache.maven.artifact.repository.MavenArtifactRepository;
import org.apache.maven.artifact.repository.layout.ArtifactRepositoryLayout;
import org.apache.maven.execution.DefaultMavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionRequest;
import org.apache.maven.execution.MavenExecutionResult;
import org.apache.maven.model.Build;
import org.apache.maven.project.DefaultProjectBuildingRequest;
import org.apache.maven.project.MavenProject;
import org.apache.maven.project.ProjectBuilder;
import org.apache.maven.project.ProjectBuildingRequest;
import org.apache.maven.project.ProjectBuildingResult;
import org.codehaus.plexus.DefaultPlexusContainer;
import org.codehaus.plexus.logging.console.ConsoleLoggerManager;
import org.junit.Before;
import org.junit.Ignore;
import org.junit.Test;

/**
 * MavenIntegrationTestCase
 *
 * @author <a href="mailto:aslak@redhat.com">Aslak Knutsen</a>
 * @version $Revision: $
 */
public class MavenIntegrationTestCase
{
   private DefaultPlexusContainer container;
   private ProjectBuilder builder;
   
   @Before
   public void startUp() throws Exception 
   {
      long start = System.currentTimeMillis();
      
      container = new DefaultPlexusContainer();
      container.setLoggerManager(new ConsoleLoggerManager("DEBUG"));
      
      builder = container.lookup(ProjectBuilder.class);
      
      System.out.println("[@Before] " + (System.currentTimeMillis() - start));
   }

   @Test
   @Ignore
   public void shouldResolveDependencies() throws Exception 
   {
      requestDependencies(new File("../extension-classloader/pom.xml"));
   }

   @Test
   @Ignore
   public void shouldPakcage() throws Exception 
   {
      build(new File("../extension-classloader/pom.xml"));
   }
   
   
   
   @Test
   @Ignore
   public void readAllTestArtifacs() throws Exception
   {
      File[] poms = new File[]{
            new File("pom.xml"),
            new File("../pom.xml"),
            new File("../extension-classloader/pom.xml"),
            new File("../extension-vdf/pom.xml"),
            new File("../extension-vfs3/pom.xml"),
            new File("../extension-openejb/pom.xml"),
            new File("../extension-glassfish/pom.xml"),
            new File("../api/pom.xml"),
            new File("../impl-base/pom.xml"),
            new File("../spi/pom.xml")
      };
      for(int i = 0; i < 10; i++)
      {
         
         File pom = poms[i];
         
         long start = System.currentTimeMillis();
         int numbersResolved = requestDependencies(pom);
         System.out.println("[" + numbersResolved + "] " + "[" + (System.currentTimeMillis() - start) + "] "  + pom.getPath());
      }
   }
   
   
   public void build(File pom) throws Exception
   {
      Maven maven = container.lookup(Maven.class);
      MavenExecutionRequest request = new DefaultMavenExecutionRequest();

      request.setLocalRepository(new MavenArtifactRepository(
            "local", 
            "file:///home/aslak/.m2/repository", 
            container.lookup(ArtifactRepositoryLayout.class), 
            new ArtifactRepositoryPolicy(true, ArtifactRepositoryPolicy.UPDATE_POLICY_NEVER, ArtifactRepositoryPolicy.CHECKSUM_POLICY_WARN), 
            new ArtifactRepositoryPolicy(true, ArtifactRepositoryPolicy.UPDATE_POLICY_NEVER, ArtifactRepositoryPolicy.CHECKSUM_POLICY_WARN)));

      request.setPom(pom);
      request.setBaseDirectory(pom.getParentFile());
      request.setGoals(Arrays.asList("package"));
      request.setInteractiveMode(false);
      request.setOffline(true);
      
      MavenExecutionResult result = maven.execute(request);
      
      MavenProject project = result.getProject();
      Build build = project.getBuild();
      
      System.out.println(build.getDirectory() + "/" + build.getFinalName() + "." + project.getPackaging());
   }
   
   public int requestDependencies(File pom) throws Exception 
   {
      ProjectBuildingRequest request = new DefaultProjectBuildingRequest();
      request.setLocalRepository(new MavenArtifactRepository(
            "local", 
            "file:///home/aslak/.m2/repository", 
            container.lookup(ArtifactRepositoryLayout.class), 
            new ArtifactRepositoryPolicy(true, ArtifactRepositoryPolicy.UPDATE_POLICY_NEVER, ArtifactRepositoryPolicy.CHECKSUM_POLICY_WARN), 
            new ArtifactRepositoryPolicy(true, ArtifactRepositoryPolicy.UPDATE_POLICY_NEVER, ArtifactRepositoryPolicy.CHECKSUM_POLICY_WARN)));
      
      request.setProcessPlugins(true);
      request.setResolveDependencies(true);
      request.setOffline(true);
      
      ProjectBuildingResult result = builder.build(pom, request);
      
      Set<Artifact> artifacts = result.getArtifactResolutionResult().getArtifacts();
      return artifacts.size();

//      System.out.println(project);
//      for(Artifact artifact : artifacts)
//      {
//         if(Artifact.SCOPE_PROVIDED.equals(artifact.getScope()))
//         {
//            continue;
//         }
//         System.out.println(artifact);
//      }
   }
}
