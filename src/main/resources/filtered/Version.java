package de.aikiit.bildbearbeiter.util;

/**
 * Contents of that file is generated during the build.
 */
public class Version {

    /**
     * Generated version constant
     */
    public static final String VERSION = "${project.version}";
    /** 20110823: maven.build.timestamp does not work with mvn 3.0.3 */
    public static final String TIMESTAMP = "${timestamp}";
    public static final String BUILD_NUMBER = "${buildNumber}";
    public static final String PROJECT_ARTIFACT_ID = "${project.artifactId}";
    public static final String PROJECT_VERSION = "${project.version}";

} 
