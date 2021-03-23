/*
Copyright 2011, Aiki IT, FotoRenamer
Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

    http://www.apache.org/licenses/LICENSE-2.0

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
*/
package de.aikiit.fotorenamer.util;

/**
 * Contents of that file is generated during the build.
 */
public class Version {

    /**
    * Current version of the project read from pom.xml.
    */
    public static final String VERSION = "${project.version}";
    /**
    * Build timestamp in milliseconds.
    */
    public static final String TIMESTAMP = "${timestamp}";
    /**
    * Build number of the current artifact.
    */
    public static final String BUILD_NUMBER = "${buildNumber}";
    /**
    * Maven coordinate: project.artifactId.
    */
    public static final String PROJECT_ARTIFACT_ID = "${project.artifactId}";
    /**
    * Maven coordinate: project.version.
    */
    public static final String PROJECT_VERSION = "${project.version}";

} 
