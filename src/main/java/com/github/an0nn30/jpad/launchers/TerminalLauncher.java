package com.github.an0nn30.jpad.launchers;

import com.jediterm.pty.PtyProcessTtyConnector;
import com.jediterm.terminal.TerminalColor;
import com.jediterm.terminal.TextStyle;
import com.jediterm.terminal.TtyConnector;
import com.jediterm.terminal.emulator.ColorPalette;
import com.jediterm.terminal.ui.JediTermWidget;
import com.jediterm.terminal.ui.settings.DefaultSettingsProvider;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

import com.pty4j.PtyProcess;
import com.pty4j.PtyProcessBuilder;

public class TerminalLauncher {

    private JediTermWidget terminalWidget;
    private TtyConnector ttyConnector;
    private PtyProcess ptyProcess; // Store the underlying process

    /**
     * Initializes the terminal widget if not already created.
     * Returns the JediTermWidget instance.
     */
    public JediTermWidget initTerminal() {
        if (terminalWidget != null) {
            return terminalWidget;
        }
        ttyConnector = createTtyConnector();
        terminalWidget = createTerminalWidget(ttyConnector);
        terminalWidget.start();
        return terminalWidget;
    }

    /**
     * Returns the terminal widget if already initialized.
     */
    public JediTermWidget getTerminalWidget() {
        return terminalWidget;
    }

    /**
     * Sends the provided command string to the terminal.
     * The string is written to the underlying shell's input stream.
     *
     * @param command the command(s) to send (include necessary newline characters).
     */
    public void sendCommand(String command) {
        if (ttyConnector == null) {
            System.err.println("Terminal not initialized. Call initTerminal() first.");
            return;
        }
        try {
            ttyConnector.write(command);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Checks whether the underlying process is currently running.
     *
     * @return true if the process is running; false otherwise.
     */
    public boolean isProcessRunning() {
        return ptyProcess != null && ptyProcess.isAlive();
    }

    /**
     * Kills the underlying process if it is running.
     * This can be used to stop the current execution.
     */
    public void killProcess() {
        if (isProcessRunning()) {
            ptyProcess.destroy();
        }
    }

    /**
     * Creates and returns a JediTermWidget using the provided TtyConnector.
     *
     * @param connector the TtyConnector for the terminal.
     * @return the created JediTermWidget.
     */
    private @NotNull JediTermWidget createTerminalWidget(@NotNull TtyConnector connector) {
        JediTermWidget widget = new JediTermWidget(80, 20, new DefaultSettingsProvider() {
            @Override
            public ColorPalette getTerminalColorPalette() {
                return new ColorPalette() {
                    // Define the dark variant of the Monokai 16-color ANSI palette.
                    private final Color[] monokaiDarkColors = new Color[] {
                            new Color(0x1E, 0x1F, 0x1E),  // 0: Dark Background
                            new Color(0xF9, 0x26, 0x72),  // 1: Red
                            new Color(0xA6, 0xE2, 0x2E),  // 2: Green
                            new Color(0xF4, 0xBF, 0x75),  // 3: Yellow
                            new Color(0x66, 0xD9, 0xEF),  // 4: Blue
                            new Color(0xAE, 0x81, 0xFF),  // 5: Magenta
                            new Color(0xA1, 0xEF, 0xE4),  // 6: Cyan
                            new Color(0xF8, 0xF8, 0xF2),  // 7: Foreground (Light)
                            new Color(0x55, 0x55, 0x55),  // 8: Bright Black (darker gray)
                            new Color(0xF9, 0x26, 0x72),  // 9: Bright Red
                            new Color(0xA6, 0xE2, 0x2E),  // 10: Bright Green
                            new Color(0xF4, 0xBF, 0x75),  // 11: Bright Yellow
                            new Color(0x66, 0xD9, 0xEF),  // 12: Bright Blue
                            new Color(0xAE, 0x81, 0xFF),  // 13: Bright Magenta
                            new Color(0xA1, 0xEF, 0xE4),  // 14: Bright Cyan
                            new Color(0xF8, 0xF8, 0xF2)   // 15: Bright White
                    };

                    @Override
                    protected @NotNull Color getForegroundByColorIndex(int colorIndex) {
                        return monokaiDarkColors[colorIndex];
                    }

                    @Override
                    protected @NotNull Color getBackgroundByColorIndex(int colorIndex) {
                        return monokaiDarkColors[colorIndex];
                    }
                };
            }

            @Override
            public TextStyle getDefaultStyle() {
                // Use the Monokai dark theme: foreground (index 7) and background (index 0)
                return new TextStyle(TerminalColor.index(7), TerminalColor.index(0));
            }
        });
;
        widget.setTtyConnector(connector);
        return widget;
    }

    /**
     * Creates a TtyConnector that launches the user's default shell.
     * On Windows, it launches "cmd.exe"; on Unix-like systems, it uses the SHELL environment variable (or defaults to /bin/sh).
     *
     * @return the created TtyConnector.
     */
    private @NotNull TtyConnector createTtyConnector() {
        try {
            Map<String, String> envs = System.getenv();
            String[] command;
            if (isWindows()) {
                command = new String[]{"cmd.exe"};
            } else {
                String shell = System.getenv("SHELL");
                if (shell == null || shell.isEmpty()) {
                    shell = "/bin/sh";
                }
                command = new String[]{shell, "--login"};
                envs = new HashMap<>(System.getenv());
                envs.put("TERM", "xterm-256color");
            }
            // Create and store the process.
            ptyProcess = new PtyProcessBuilder()
                    .setCommand(command)
                    .setEnvironment(envs)
                    .start();
            return new PtyProcessTtyConnector(ptyProcess, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    /**
     * Utility method to check if the OS is Windows.
     */
    private boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }
}