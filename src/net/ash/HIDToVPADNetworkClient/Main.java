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

import net.ash.HIDToVPADNetworkClient.gui.GuiMain;
import net.ash.HIDToVPADNetworkClient.tui.TuiMain;
import net.ash.HIDToVPADNetworkClient.manager.ActiveControllerManager;
import net.ash.HIDToVPADNetworkClient.network.NetworkManager;
import net.ash.HIDToVPADNetworkClient.util.MessageBoxManager;
import net.ash.HIDToVPADNetworkClient.util.Settings;

/* Ash's todo list
 * TODO finish HidController
 * TODO locale
 */
public final class Main {
    private static boolean tuiMode = false;
    
    public static void main(String[] args) {
        // Check for TUI mode argument
        for (String arg : args) {
            if ("--tui".equals(arg) || "--no-gui".equals(arg) || "-t".equals(arg)) {
                tuiMode = true;
                break;
            } else if ("--help".equals(arg) || "-h".equals(arg)) {
                printHelp();
                return;
            }
        }
        
        Settings.loadSettings();
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
            tuiMain.start();
        } else {
            // Run in GUI mode
            SwingUtilities.invokeLater(new Runnable() {
                public void run() {
                    GuiMain.getInstance();

                }
            });

            MessageBoxManager.addMessageBoxListener(GuiMain.getInstance());
        }
    }
    
    private static void printHelp() {
        System.out.println("HID To VPAD Network Client");
        System.out.println();
        System.out.println("Usage: java -jar HIDToVPADNetworkClient.jar [OPTIONS]");
        System.out.println();
        System.out.println("Options:");
        System.out.println("  --tui, -t, --no-gui    Run in text-based user interface mode (no GUI)");
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
