# Makefile for building, packaging, and signing RetroEdit app
# Extract the version from pom.xml.
# If xmllint isn't available on your system, you could instead use:
# VERSION := $(shell sed -n 's/.*<version>\(.*\)<\/version>.*/\1/p' pom.xml | head -n 1)
# Extract the version from pom.xml using sed (xmllint may not be available on Linux)
VERSION := $(shell sed -n 's/.*<version>\(.*\)<\/version>.*/\1/p' pom.xml | head -n 1)

# Variables
APP_NAME = jPad
MAIN_JAR = jpad-$(VERSION)-jar-with-dependencies.jar
MAIN_CLASS = com.github.an0nn30.jpad.Main
ICON_PATH_MAC = src/main/resources/jpad.icns
ICON_PATH_WIN = src/main/resources/jpa.ico
ICON_PATH_LINUX = src/main/resources/jpad.png
APP_VERSION = $(VERSION)
VENDOR = "An0nn30"
OUTPUT_DIR = output/
TARGET_DIR = target/
DEV_ID = "Developer ID Application: Dustin McKennaWatts (G8Y2U3KKZR)"
APP_PATH = $(OUTPUT_DIR)$(APP_NAME).app
FILE_ASSOC = file-associations.properties

# Default target
.PHONY: all
all: jar file-associations deploy sign

# Build the JAR using Maven
.PHONY: jar
jar:
	mvn clean package

# Generate file associations for code files if it doesn't exist.
# This file will be used by jpackage to register common code file associations.
$(FILE_ASSOC):
	@echo "Generating file associations file..."
	@echo "# File Associations for $(APP_NAME)" > $(FILE_ASSOC)
	@echo "" >> $(FILE_ASSOC)
	@echo "association.java.extensions=java" >> $(FILE_ASSOC)
	@echo "association.java.mime-type=text/x-java-source" >> $(FILE_ASSOC)
	@echo "association.java.description=Java Source Files" >> $(FILE_ASSOC)
	@echo "" >> $(FILE_ASSOC)
	@echo "association.c.extensions=c" >> $(FILE_ASSOC)
	@echo "association.c.mime-type=text/x-c" >> $(FILE_ASSOC)
	@echo "association.c.description=C Source Files" >> $(FILE_ASSOC)
	@echo "" >> $(FILE_ASSOC)
	@echo "association.cpp.extensions=cpp;h;hpp" >> $(FILE_ASSOC)
	@echo "association.cpp.mime-type=text/x-c++;text/x-c-header" >> $(FILE_ASSOC)
	@echo "association.cpp.description=C/C++ Source Files" >> $(FILE_ASSOC)
	@echo "" >> $(FILE_ASSOC)
	@echo "association.json.extensions=json" >> $(FILE_ASSOC)
	@echo "association.json.mime-type=application/json" >> $(FILE_ASSOC)
	@echo "association.json.description=JSON Files" >> $(FILE_ASSOC)
	@echo "" >> $(FILE_ASSOC)
	@echo "association.yaml.extensions=yaml;yml" >> $(FILE_ASSOC)
	@echo "association.yaml.mime-type=application/x-yaml" >> $(FILE_ASSOC)
	@echo "association.yaml.description=YAML Files" >> $(FILE_ASSOC)
	@echo "" >> $(FILE_ASSOC)
	@echo "association.python.extensions=py" >> $(FILE_ASSOC)
	@echo "association.python.mime-type=text/x-python" >> $(FILE_ASSOC)
	@echo "association.python.description=Python Files" >> $(FILE_ASSOC)
	@echo "" >> $(FILE_ASSOC)
	@echo "association.go.extensions=go" >> $(FILE_ASSOC)
	@echo "association.go.mime-type=text/x-go" >> $(FILE_ASSOC)
	@echo "association.go.description=Go Files" >> $(FILE_ASSOC)

# Create the macOS app using jpackage and include file associations
.PHONY: deploy
deploy: jar $(FILE_ASSOC)
	@rm -rf $(OUTPUT_DIR)
	jpackage --type app-image \
	  --name $(APP_NAME) \
	  --input $(TARGET_DIR) \
	  --main-jar $(MAIN_JAR) \
	  --main-class $(MAIN_CLASS) \
	  --java-options '--enable-preview' \
	  --icon $(ICON_PATH_MAC) \
	  --app-version $(APP_VERSION) \
	  --vendor $(VENDOR) \
	  --file-associations $(FILE_ASSOC) \
	  --dest $(OUTPUT_DIR) \
	  --verbose

# Create a deb package (Linux) using jpackage and include file associations
.PHONY: deb
deb: jar $(FILE_ASSOC)
	@rm -rf $(OUTPUT_DIR)
	jpackage --type deb \
	  --name $(APP_NAME) \
	  --input $(TARGET_DIR) \
	  --main-jar $(MAIN_JAR) \
	  --main-class $(MAIN_CLASS) \
	  --app-version $(APP_VERSION) \
	  --vendor $(VENDOR) \
	  --icon $(ICON_PATH_LINUX) \
	  --linux-shortcut \
	  --resource-dir linux/ \
	  --file-associations $(FILE_ASSOC) \
	  --dest $(OUTPUT_DIR) \
	  --verbose

# Sign the application with Apple's Developer ID
#.PHONY: sign
#sign: deploy
#	codesign --deep --force --verbose \
#	  --sign "$(DEV_ID)" \
#	  $(APP_PATH)

# Create a DMG with the signed .app file (macOS) using jpackage and include file associations
.PHONY: dmg
dmg: deploy $(FILE_ASSOC)
	jpackage --type dmg \
	  --name $(APP_NAME) \
	  --app-image $(APP_PATH) \
	  --icon $(ICON_PATH_MAC) \
	  --app-version $(APP_VERSION) \
	  --vendor $(VENDOR) \
	  --file-associations $(FILE_ASSOC) \
	  --dest $(OUTPUT_DIR) \
	  --verbose

# Create a Windows EXE installer using jpackage and include file associations
.PHONY: exe
exe: deploy $(FILE_ASSOC)
	jpackage --type exe \
	  --name $(APP_NAME) \
	  --app-image $(APP_PATH) \
	  --icon $(ICON_PATH_WIN) \
	  --app-version $(APP_VERSION) \
	  --vendor $(VENDOR) \
	  --file-associations $(FILE_ASSOC) \
	  --dest $(OUTPUT_DIR) \
	  --verbose

# Clean generated files
.PHONY: clean
clean:
	mvn clean
	rm -rf $(OUTPUT_DIR) $(FILE_ASSOC)