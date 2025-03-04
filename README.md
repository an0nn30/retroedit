# Retro Edit
![alt text](https://github.com/an0nn30/retroedit/blob/main/screenshot.png?raw=true)
![alt text](https://github.com/an0nn30/retroedit/blob/main/screenshot2.png?raw=true)


Retro Edit is a lightweight, customizable code editor built in Java using Swing. It provides a modern, efficient coding environment with advanced features such as syntax highlighting, code folding, auto-completion, and an integrated file manager. The application uses a modular architecture with decoupled components that communicate via an event bus, making it easy to extend and maintain.

## Features

- **Tabbed Editing:**  
  Open, close, and manage multiple files in separate tabs.

- **Syntax Highlighting & Code Folding:**  
  Supports multiple languages including Java, Python, C, C++, JSON, and Go using RSyntaxTextArea.

- **Auto-Completion:**  
  Provides basic auto-completion for Java keywords and shorthand completions (e.g., `sysout` for `System.out.println`).

- **Find/Replace & Go-To Line:**  
  Built-in dialogs and actions for searching, replacing, and navigating to specific lines in your code.

- **File Management:**
    - **Directory Tree:** Browse files and directories with drag-and-drop support and context menus.
    - **File Open/Save:** Choose files using native file dialogs or custom dialogs based on the interface theme.

- **Dynamic Theming:**  
  Switch between themes (e.g., retro/light/dark) dynamically. The ThemeManager applies the appropriate look-and-feel and icon resources.

- **Custom Logging & Settings:**
    - **Logging:** A lightweight logging system that prints messages to the console.
    - **Settings:** A JSON-based configuration system that allows you to customize fonts, themes, and other options.

## Technology Stack

- **Java 11+**
- **Swing** – For building the UI.
- **RSyntaxTextArea** – For syntax highlighting and code editing.
- **FlatLaf** – A modern Look & Feel for Swing.
- **Gson** – For JSON serialization/deserialization of settings.
- **Maven** – For dependency management and build automation.

## Project Structure

- **`com.github.an0nn30.jpad.ui`**  
  Core UI components including the main editor frame, menu bar, and status panel.

- **`com.github.an0nn30.jpad.ui.actions`**  
  Action classes for commands such as Find, Replace, and Go-To Line.

- **`com.github.an0nn30.jpad.ui.components`**  
  Custom Swing components:
    - **`TextArea`** – An extended RSyntaxTextArea with auto-completion and dynamic settings updates.
    - **`DirectoryTree`** – A file tree with drag-and-drop and context menu support.
    - **`Panel`** – A custom panel with pre-defined layouts and utility methods.
    - **`StatusPanel`** – Displays terminal toggling, file type selection, and status information.

- **`com.github.an0nn30.jpad.ui.search`**  
  Contains find and replace dialogs and toolbars for search functionality.

- **`com.github.an0nn30.jpad.ui.theme`**  
  Manages themes and icon resources.

- **`com.github.an0nn30.jpad.ui.utils`**  
  Utility classes for file management and common UI operations.

- **`com.github.an0nn30.jpad.logging`**  
  Custom logging implementation.

- **`com.github.an0nn30.jpad.event`**  
  A simple event bus for decoupled communication between components.

- **`com.github.an0nn30.jpad.settings`**  
  Application settings management.

## How to Run

1. **Clone the Repository:**

   ```bash
   git clone https://github.com/yourusername/retroedit.git
   cd retroedit
   ```
2. **Build the Project**
    ```bash
   mvn clean package
   ```
3. **Run!**
   ```bash
   java -jar target/retroedit-$VERSION$.jar
   ```
   
## Configuration and Settings

The application settings are managed via a JSON file (typically settings.json) stored in the user’s configuration directory. You can customize:
-	Interface Theme: Switch between “retro”, “light”, or “dark”.
-	Font Settings: Configure the editor and interface font families and sizes.
-	Log Level: Set the desired logging level (DEBUG, INFO, WARN, ERROR).

A dedicated settings dialog is provided within the application for easy configuration.


## Contribution
1.	Fork the repository.
2.	Create a new branch for your feature or bugfix.
3.	Write tests where applicable.
4.	Submit a pull request detailing your changes.

## License
This project is licensed under the MIT License. See the LICENSE file for details.