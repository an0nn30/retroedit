# Retro Edit

### What is it?
Retro edit aims to be a feature-complete text editor written entirely in Java/Swing. It started out as a project that I was dabbling in out of bordom and wanting to make something, and turned into an editor that is exactly what I want in an editor. 

When I say aims to be "feature-complete", what I mean is that it will or already has most features you'd find in a common editor. Syntax highlighting, code completion, and basic build support. 

### How to build?
It's using Maven, so just make sure you've got jdk version 23 installed, and you can import the maven project into InteliJ or your IDE of choice. 

-- OR --

To build from the command line, you can just run "mvn clean package" to build the jar file. I use a mac, so generally I like to build a Mac image with my Makefile: `make dmg` and use it that way. 

PR's are welcome, just make an issue so I know what you're planning on adding and assign it to yourself. 

As I think of a feature, I also create an issue with the intention of adding that feature later. 

