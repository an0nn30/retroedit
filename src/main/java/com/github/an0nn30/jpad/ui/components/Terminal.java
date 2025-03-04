package com.github.an0nn30.jpad.ui.components;

import com.github.an0nn30.jpad.ui.EditorFrame;
import com.jediterm.pty.PtyProcessTtyConnector;
import com.jediterm.terminal.TerminalColor;
import com.jediterm.terminal.TextStyle;
import com.jediterm.terminal.TtyConnector;
import com.jediterm.terminal.emulator.ColorPalette;
import com.jediterm.terminal.ui.JediTermWidget;
import com.jediterm.terminal.ui.settings.DefaultSettingsProvider;
import com.pty4j.PtyProcess;
import com.pty4j.PtyProcessBuilder;
import org.jetbrains.annotations.NotNull;

import java.awt.*;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;

public class Terminal {


    public static @NotNull JediTermWidget createTerminalWidget(EditorFrame editorFrame) {
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
        widget.setTtyConnector(createTtyConnector());
        widget.start();
        return widget;
    }

    private static @NotNull TtyConnector createTtyConnector() {
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

            PtyProcess process = new PtyProcessBuilder()
                    .setCommand(command)
                    .setEnvironment(envs)
                    .start();
            return new PtyProcessTtyConnector(process, StandardCharsets.UTF_8);
        } catch (Exception e) {
            throw new IllegalStateException(e);
        }
    }

    private static boolean isWindows() {
        return System.getProperty("os.name").toLowerCase().contains("win");
    }
}