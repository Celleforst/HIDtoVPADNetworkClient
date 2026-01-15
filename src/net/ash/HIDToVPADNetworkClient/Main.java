/*******************************************************************************
 * Copyright (c) 2017 Ash (QuarkTheAwesome) & Maschell
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 *******************************************************************************/
package net.ash.HIDToVPADNetworkClient;

import javax.swing.SwingUtilities;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.ash.HIDToVPADNetworkClient.gui.GuiMain;
import net.ash.HIDToVPADNetworkClient.tui.TuiMain;
import net.ash.HIDToVPADNetworkClient.manager.ActiveControllerManager;
import net.ash.HIDToVPADNetworkClient.network.NetworkManager;
import net.ash.HIDToVPADNetworkClient.util.MessageBoxManager;
import net.ash.HIDToVPADNetworkClient.util.MessageBox;
import net.ash.HIDToVPADNetworkClient.util.Settings;

/* Ash's todo list
 * TODO finish HidController
 * TODO locale
 */
public final class Main {
    private static boolean tuiMode = false;
    private static boolean debugMode = false;
    private static boolean autoConnect = false;
    private static boolean headlessMode = false;
    
    public static void main(String[] args) {
        // Check for TUI and debug mode arguments
        for (String arg : args) {
            if ("--tui".equals(arg) || "--no-gui".equals(arg) || "-t".equals(arg)) {
                tuiMode = true;
            } else if ("--help".equals(arg) || "-h".equals(arg)) {
                printHelp();
                return;
            } else if ("--debug".equals(arg) || "--tui-debug".equals(arg) || "-d".equals(arg)) {
                debugMode = true;
                net.ash.HIDToVPADNetworkClient.util.Settings.DEBUG_TUI = true;
                net.ash.HIDToVPADNetworkClient.util.Settings.DEBUG_TCP_PING_PONG = true;
            } else if ("--headless".equals(arg) || "--daemon".equals(arg) || "--no-menu".equals(arg)) {
                headlessMode = true;
                net.ash.HIDToVPADNetworkClient.util.Settings.HEADLESS_TUI = true;
            } else if ("--auto-connect".equals(arg) || "--autoconnect".equals(arg) || "-a".equals(arg)) {
                autoConnect = true;
            }
        }
        
        Settings.loadSettings();
        // Respect the user's --auto-connect flag so background checks activate
        net.ash.HIDToVPADNetworkClient.util.Settings.AUTO_CONNECT_ON_START = autoConnect;
        // Configure logging for TUI mode: keep terminal clean unless debug enabled
        if (tuiMode) {
            Logger root = Logger.getLogger("");
            if (!debugMode) {
                root.setLevel(Level.WARNING);
                for (Handler h : root.getHandlers()) {
                    h.setLevel(Level.WARNING);
                }
            } else {
                root.setLevel(Level.ALL);
                for (Handler h : root.getHandlers()) {
                    h.setLevel(Level.ALL);
                }
            }
        }

        // Force JNA class initialization early so any JVM/JNA startup warnings
        // (which go to stderr) appear before the TUI menu output instead of
        // being printed later underneath it.
        try {
            Class.forName("com.sun.jna.Native", true, Main.class.getClassLoader());
        } catch (Throwable t) {
            // ignore if JNA not present or initialization fails
        }
        try {
            new Thread(ActiveControllerManager.getInstance(), "ActiveControllerManager").start();
            new Thread(NetworkManager.getInstance(), "NetworkManager").start();
        } catch (Exception e) {
            e.printStackTrace();
            fatal();
        }
        
        if (tuiMode) {
            // Run in TUI mode
            TuiMain tuiMain = TuiMain.getInstance();
            MessageBoxManager.addMessageBoxListener(tuiMain);
            if (autoConnect) {
                // Attempt to connect before showing the menu and display result immediately
                boolean connected = NetworkManager.getInstance().connect(Settings.getIpAddr());
                if (connected) {
                    tuiMain.showMessageBox(new MessageBox("Auto-connect: Connected to " + Settings.getIpAddr(), MessageBox.MESSAGE_INFO));
                } else {
                    tuiMain.showMessageBox(new MessageBox("Auto-connect: Failed to connect to " + Settings.getIpAddr(), MessageBox.MESSAGE_WARNING));
                }
            }
            if (headlessMode) {
                tuiMain.startHeadless();
            } else {
                tuiMain.start();
            }
        } else {
            // Run in GUI mode
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    GuiMain.getInstance();

                }
            });

            MessageBoxManager.addMessageBoxListener(GuiMain.getInstance());
        }

        // (Auto-connect handled above for TUI when requested.)
    }
    
    private static void printHelp() {
        System.out.println("HID To VPAD Network Client");
        System.out.println();
        System.out.println("Usage: java -jar HIDToVPADNetworkClient.jar [OPTIONS]");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  --tui, -t, --no-gui    Run in text-based user interface mode (no GUI)");
        System.out.println("  --auto-connect, -a     Attempt to connect to configured IP before showing TUI menu");
        System.out.println("  --debug, -d            Enable TUI debug messages (device add/remove notifications)");
        System.out.println("  --help, -h             Show this help message");
        System.out.println();
        System.out.println("If no options are specified, the application runs in GUI mode.");
    }

    private Main() {
    }

    public static void fatal() {
        System.err.println("HID To VPAD Network Client encountered an irrecoverable error.");
        System.err.println("Exiting...");
        System.exit(1);
    }

    public static void initiateShutdown() {
        System.exit(0);
    }
}
