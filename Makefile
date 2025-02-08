# Makefile for building, packaging, and signing TheEditor app

# Variables
APP_NAME = TheEditor
MAIN_JAR = editor-1.0-SNAPSHOT-jar-with-dependencies.jar
MAIN_CLASS = com.github.an0nn30.Main
ICON_PATH = src/main/resources/the-editor.icns
APP_VERSION = 1.0.0
VENDOR = "An0nn30"
OUTPUT_DIR = output/
TARGET_DIR = target/
DEV_ID = "Developer ID Application: Dustin McKennaWatts (G8Y2U3KKZR)"
APP_PATH = $(OUTPUT_DIR)$(APP_NAME).app

# Default target
.PHONY: all
all: jar deploy sign

# Build the JAR using Maven
.PHONY: jar
jar:
	mvn clean package

# Create the macOS app using jpackage
.PHONY: deploy
deploy: jar
	rm -rf output && \
	jpackage --type app-image \
	  --name $(APP_NAME) \
	  --input $(TARGET_DIR) \
	  --main-jar $(MAIN_JAR) \
	  --main-class $(MAIN_CLASS) \
	  --java-options '--enable-preview' \
	  --icon $(ICON_PATH) \
	  --app-version $(APP_VERSION) \
	  --vendor $(VENDOR) \
	  --dest $(OUTPUT_DIR) \
	  --verbose

# Sign the application with Apple's Developer ID
.PHONY: sign
sign: deploy
	codesign --deep --force --verbose \
	  --sign "$(DEV_ID)" \
	  $(APP_PATH)

# Clean generated files
.PHONY: clean
clean:
	mvn clean
	rm -rf $(OUTPUT_DIR)
