/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.activemq.protobuf.compiler;

import java.io.File;
import java.io.FileFilter;
import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import org.apache.activemq.protobuf.compiler.JavaGenerator.CompilerException;
import org.apache.activemq.protobuf.compiler.parser.ParseException;
import org.apache.maven.plugin.AbstractMojo;
import org.apache.maven.plugin.MojoExecutionException;
import org.apache.maven.project.MavenProject;

/**
 * A Maven Mojo so that the Proto compiler can be used with maven.
 * 
 * @goal compile
 * @phase process-sources
 */
public class ProtoMojo extends AbstractMojo {

    /**
     * The maven project.
     * 
     * @parameter expression="${project}"
     * @required
     * @readonly
     */
    protected MavenProject project;

    /**
     * The directory where the proto files (<code>*.proto</code>) are
     * located.
     * 
     * @parameter expression="${sourceDirectory}" default-value="${basedir}/src/main/proto"
     */
    private File sourceDirectory;

    /**
     * The directory where the output files will be located.
     * 
     * @parameter expression="${outputDirectory}" default-value="${project.build.directory}/generated-sources/proto"
     */
    private File outputDirectory;

    public void execute() throws MojoExecutionException {

        File[] files = sourceDirectory.listFiles(new FileFilter() {
            public boolean accept(File pathname) {
                return pathname.getName().endsWith(".proto");
            }
        });
        
        if (files==null || files.length==0) {
            getLog().warn("No proto files found in directory: " + sourceDirectory.getPath());
            return;
        }
        
        List<File> recFiles = Arrays.asList(files);
        for (File file : recFiles) {
            try {
                getLog().info("Compiling: "+file.getPath());
                JavaGenerator generator = new JavaGenerator();
                generator.setOutputDirectory(outputDirectory);
                generator.compile(file);
            } catch (ParseException e) {
                throw new MojoExecutionException("Parse failed: "+file.getPath()+"\n"+ e.getMessage(), e);
            } catch (CompilerException e) {
                throw new MojoExecutionException("Compile failed", e);
            } catch (IOException e) {
                throw new MojoExecutionException("IO Error occured: " + e.getMessage(), e);
            }
        }

        this.project.addCompileSourceRoot(outputDirectory.getAbsolutePath());
    }

}
