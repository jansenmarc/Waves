resolvers ++= Seq(
  "Typesafe repository" at "http://repo.typesafe.com/typesafe/releases/",
  "Artima Maven Repository" at "http://repo.artima.com/releases",
  "JBoss" at "https://repository.jboss.org"
)

addSbtPlugin("com.eed3si9n" % "sbt-assembly" % "0.14.3")

addSbtPlugin("com.typesafe.sbt" % "sbt-native-packager" % "1.2.0-M8")

addSbtPlugin("org.scalastyle" %% "scalastyle-sbt-plugin" % "0.8.0")

addSbtPlugin("org.scoverage" % "sbt-scoverage" % "1.5.0")

addSbtPlugin("com.github.gseitz" % "sbt-release" % "1.0.3")

addSbtPlugin("se.marcuslonnberg" % "sbt-docker" % "1.4.1")

libraryDependencies ++= Seq(
  "com.typesafe" % "config" % "1.3.0",
  "org.vafer" % "jdeb" % "1.5" artifacts Artifact("jdeb", "jar", "jar"))
