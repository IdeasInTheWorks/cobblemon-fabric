// Independent ports publish validated binaries only through IdeasInTheWorks GitHub Releases.
// No upstream Maven publication destinations or credentials are configured here.
plugins {
    id("java-library")
    id("maven-publish")
}

publishing {
    publications {
        create<MavenPublication>(project.name) {
            from(components["java"])
        }
    }
}
