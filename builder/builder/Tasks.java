/*
 * Author: Blake McBride
 * Date: 2/16/20
 *
 * I've found that I spend more time messing with build programs (such as Maven, Gradle, and others) than
 * the underlying application I am trying to build.  They all do the normal things very, very easily.
 * But when you try to go off their beaten path it gets real difficult real fast.  Being sick and
 * tired of this, and having easily built a shell script to build what I want, I needed a more portable
 * solution.  The files in this directory are that solution.
 *
 * There are two classes as follows:
 *
 *     BuildUtils -  the generic utilities needed to build
 *     Tasks      -  the application-specific build procedures (or tasks)
 *
 *    Non-private instance methods with no parameters are considered tasks.
 */


package builder;

import static builder.BuildUtils.*;

public class Tasks {

    final String LIBS = "libs";
    final ForeignDependencies foreignLibs = buildForeignDependencies();
    final LocalDependencies localLibs = buildLocalDependencies();
    final String BUILDDIR = "build.work";
    final String explodedDir = BUILDDIR + "/" + "exploded";

    void all() {
        war();
        javadoc();
    }

    void libs() {
        downloadAll(foreignLibs);
    }

    void war() {
        libs();
        writeToFile(explodedDir + "/META-INF/MANIFEST.MF", "Manifest-Version: 1.0\n");
        copyTree("src/main/application", explodedDir + "/WEB-INF/application");
        copyTree("libs", explodedDir + "/WEB-INF/lib");
        buildJava("src/main/java", explodedDir + "/WEB-INF/classes", localLibs, foreignLibs);
        rm(explodedDir + "/WEB-INF/lib/javax.servlet-api-4.0.1.jar");
        copyRegex("src/main/java/org/kissweb/lisp", explodedDir + "/WEB-INF/classes/org/kissweb/lisp", ".*\\.lisp", null, false);
        createJar(explodedDir, BUILDDIR + "/ExtraAbilities.war");
        //println("ExtraAbilities.war has been created in the " + BUILDDIR + " directory");
    }

    void javadoc() {
        buildJavadoc("src/main/java", "libs", BUILDDIR + "/javadoc");
    }

    void clean() {
        rmTree(BUILDDIR);
    }

    void realclean() {
        clean();
        rmTree("src/main/webapp/lib");
        delete(foreignLibs);

        rmRegex("builder/builder", ".*\\.class");

        rmTree(".project");
        rmTree(".settings");
        rmTree(".vscode");

        // intelliJ
        //rmTree(".idea");
        rmTree("out");
        //rmRegex(".", ".*\\.iml");
	
        // NetBeans
        rmTree("dist");
        rmTree("nbproject");
        rmTree("build");
        rm("nbbuild.xml");
    }

    private ForeignDependencies buildForeignDependencies() {
        final ForeignDependencies dep = new ForeignDependencies();
        dep.add("c3p0-0.9.5.5.jar", LIBS, "https://repo1.maven.org/maven2/com/mchange/c3p0/0.9.5.5/c3p0-0.9.5.5.jar");
        dep.add("groovy-all-2.4.18.jar", LIBS, "https://repo1.maven.org/maven2/org/codehaus/groovy/groovy-all/2.4.18/groovy-all-2.4.18.jar");
        dep.add("javax.servlet-api-4.0.1.jar", LIBS, "https://repo1.maven.org/maven2/javax/servlet/javax.servlet-api/4.0.1/javax.servlet-api-4.0.1.jar");
        dep.add("javax.mail-1.6.1.jar", LIBS, "https://repo1.maven.org/maven2/com/sun/mail/javax.mail/1.6.1/javax.mail-1.6.1.jar");
        dep.add("log4j-1.2.17.jar", LIBS, "https://repo1.maven.org/maven2/log4j/log4j/1.2.17/log4j-1.2.17.jar");
        dep.add("mchange-commons-java-0.2.20.jar", LIBS, "https://repo1.maven.org/maven2/com/mchange/mchange-commons-java/0.2.20/mchange-commons-java-0.2.20.jar");
        dep.add("postgresql-42.2.10.jar", LIBS, "https://repo1.maven.org/maven2/org/postgresql/postgresql/42.2.10/postgresql-42.2.10.jar");
        dep.add("slf4j-api-1.7.30.jar", LIBS, "https://repo1.maven.org/maven2/org/slf4j/slf4j-api/1.7.30/slf4j-api-1.7.30.jar");
        dep.add("slf4j-simple-1.7.30.jar", LIBS, "https://repo1.maven.org/maven2/org/slf4j/slf4j-simple/1.7.30/slf4j-simple-1.7.30.jar");
        dep.add("jquery-3.4.1.min.js", "src/main/webapp/lib", "https://ajax.googleapis.com/ajax/libs/jquery/3.4.1/jquery.min.js");
        return dep;
    }

    private LocalDependencies buildLocalDependencies() {
        final LocalDependencies dep = new LocalDependencies();
        dep.add("libs/abcl.jar");
        dep.add("libs/dynamic-loader-1.4-SNAPSHOT.jar");
        return dep;
    }

}
