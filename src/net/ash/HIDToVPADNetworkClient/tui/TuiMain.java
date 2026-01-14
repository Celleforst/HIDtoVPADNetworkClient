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
package net.ash.HIDToVPADNetworkClient.tui;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.List;
import java.util.Scanner;

import lombok.extern.java.Log;
import net.ash.HIDToVPADNetworkClient.controller.Controller;
import net.ash.HIDToVPADNetworkClient.manager.ControllerManager;
import net.ash.HIDToVPADNetworkClient.network.NetworkManager;
import net.ash.HIDToVPADNetworkClient.util.MessageBox;
import net.ash.HIDToVPADNetworkClient.util.MessageBoxListener;
import net.ash.HIDToVPADNetworkClient.util.Settings;

@Log
public class TuiMain implements MessageBoxListener {
    private static TuiMain instance;
    private Scanner scanner;
    private volatile boolean running = true;
    private Thread inputThread;
    private volatile String lastNotification = null;
    private final Object notificationLock = new Object();

    private TuiMain() {
        scanner = new Scanner(System.in);
    }

    public synchronized static TuiMain getInstance() {
        if (instance == null) {
            instance = new TuiMain();
        }
        return instance;
    }

    public void start() {
        printWelcome();
        
        inputThread = new Thread(() -> {
            while (running) {
                try {
                    printMenu();
                    String input = scanner.nextLine();
                    handleInput(input);
                } catch (Exception e) {
                    log.severe("Error processing input: " + e.getMessage());
                }
            }
        }, "TUI Input Thread");
        inputThread.start();
    }

    /**
     * Start the TUI in headless mode: do not show any menu or prompt, just run
     * the background managers and wait until shutdown. Useful for systemd.
     */
    public void startHeadless() {
        // Do not print the welcome or menu
        running = true;

        // Install a shutdown hook so we can perform a clean disconnect
        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            running = false;
            try {
                NetworkManager.getInstance().disconnect();
            } catch (Exception e) {
                log.warning("Error during shutdown disconnect: " + e.getMessage());
            }
        }, "TUI-Headless-Shutdown"));

        // In headless mode, periodically wake up to allow MessageBox notifications
        // to be handled and to keep main thread alive.
        while (running) {
            try {
                // Optionally perform periodic tasks here (e.g., auto-scan controllers)
                if (Settings.SCAN_AUTOMATICALLY_FOR_CONTROLLERS) {
                    ControllerManager.detectControllers();
                }
                Thread.sleep(1000);
            } catch (InterruptedException e) {
                // continue loop until running is false
            }
        }
    }

    private void printWelcome() {
        System.out.println("=====================================");
        System.out.println("HID To VPAD Network Client (TUI Mode)");
        System.out.println("=====================================");
        System.out.println("IP Address: " + Settings.getIpAddr());
        System.out.println("Connected: " + (NetworkManager.getInstance().isConnected() ? "Yes" : "No"));
        System.out.println();
    }

    private void printMenu() {
        // Print last notification (if any) above the menu so it doesn't get buried
        synchronized (notificationLock) {
            if (lastNotification != null) {
                System.out.println();
                System.out.println(lastNotification);
                lastNotification = null;
            }
        }
        System.out.println("\n--- Main Menu ---");
        System.out.println("1. List Controllers");
        System.out.println("2. Activate Controller");
        System.out.println("3. Deactivate Controller");
        System.out.println("4. Show Status");
        System.out.println("5. Change IP Address");
        System.out.println("6. Reconnect");
        System.out.println("7. Toggle Auto-scan");
        System.out.println("8. Quit");
        System.out.print("Enter choice: ");
    }

    private void handleInput(String input) {
        input = input.trim();
        
        switch (input) {
            case "1":
                listControllers();
                break;
            case "2":
                activateController();
                break;
            case "3":
                deactivateController();
                break;
            case "4":
                showStatus();
                break;
            case "5":
                changeIPAddress();
                break;
            case "6":
                reconnect();
                break;
            case "7":
                toggleAutoScan();
                break;
            case "8":
                quit();
                break;
            default:
                System.out.println("Invalid choice. Please try again.");
        }
    }

    private void listControllers() {
        System.out.println("\n--- Attached Controllers ---");
        // Force a scan so hot-plugged controllers are detected immediately in TUI
        ControllerManager.detectControllers();
        List<Controller> controllers = ControllerManager.getAttachedControllers();
        
        if (controllers.isEmpty()) {
            System.out.println("No controllers detected.");
            System.out.println("Make sure controllers are connected and scanning is enabled.");
        } else {
            for (int i = 0; i < controllers.size(); i++) {
                Controller c = controllers.get(i);
                String status = c.isActive() ? "[ACTIVE]" : "[INACTIVE]";
                System.out.println((i + 1) + ". " + status + " " + c.getIdentifier() + " - " + c.getType());
            }
        }
        System.out.println("Total: " + controllers.size() + " controller(s)");
    }

    private void activateController() {
        List<Controller> controllers = ControllerManager.getAttachedControllers();
        
        if (controllers.isEmpty()) {
            System.out.println("No controllers available to activate.");
            return;
        }
        
        System.out.println("\n--- Activate Controller ---");
        for (int i = 0; i < controllers.size(); i++) {
            Controller c = controllers.get(i);
            String status = c.isActive() ? "[ACTIVE]" : "[INACTIVE]";
            System.out.println((i + 1) + ". " + status + " " + c.getIdentifier());
        }
        
        System.out.print("Enter controller number (0 to cancel): ");
        try {
            String input = scanner.nextLine().trim();
            int choice = Integer.parseInt(input);
            
            if (choice == 0) {
                return;
            }
            
            if (choice < 1 || choice > controllers.size()) {
                System.out.println("Invalid selection.");
                return;
            }
            
            Controller selected = controllers.get(choice - 1);
            selected.setActive(true);
            System.out.println("Activated: " + selected.getIdentifier());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a number.");
        }
    }

    private void deactivateController() {
        List<Controller> controllers = ControllerManager.getActiveControllers();
        
        if (controllers.isEmpty()) {
            System.out.println("No active controllers to deactivate.");
            return;
        }
        
        System.out.println("\n--- Deactivate Controller ---");
        for (int i = 0; i < controllers.size(); i++) {
            Controller c = controllers.get(i);
            System.out.println((i + 1) + ". " + c.getIdentifier());
        }
        System.out.println((controllers.size() + 1) + ". Deactivate ALL");
        
        System.out.print("Enter controller number (0 to cancel): ");
        try {
            String input = scanner.nextLine().trim();
            int choice = Integer.parseInt(input);
            
            if (choice == 0) {
                return;
            }
            
            if (choice == controllers.size() + 1) {
                ControllerManager.deactivateAllAttachedControllers();
                System.out.println("Deactivated all controllers.");
                return;
            }
            
            if (choice < 1 || choice > controllers.size()) {
                System.out.println("Invalid selection.");
                return;
            }
            
            Controller selected = controllers.get(choice - 1);
            selected.setActive(false);
            System.out.println("Deactivated: " + selected.getIdentifier());
        } catch (NumberFormatException e) {
            System.out.println("Invalid input. Please enter a number.");
        }
    }

    private void showStatus() {
        System.out.println("\n--- System Status ---");
        System.out.println("IP Address: " + Settings.getIpAddr());
        System.out.println("Connected to Wii U: " + (NetworkManager.getInstance().isConnected() ? "Yes" : "No"));
        System.out.println("Auto-scan controllers: " + (Settings.SCAN_AUTOMATICALLY_FOR_CONTROLLERS ? "Enabled" : "Disabled"));
        System.out.println("Auto-activate controllers: " + (Settings.AUTO_ACTIVATE_CONTROLLER ? "Enabled" : "Disabled"));
        
        List<Controller> attached = ControllerManager.getAttachedControllers();
        List<Controller> active = ControllerManager.getActiveControllers();
        System.out.println("Controllers attached: " + attached.size());
        System.out.println("Controllers active: " + active.size());
    }

    private void changeIPAddress() {
        System.out.println("\n--- Change IP Address ---");
        System.out.println("Current IP: " + Settings.getIpAddr());
        System.out.print("Enter new IP address (or press Enter to cancel): ");
        
        String newIP = scanner.nextLine().trim();
        if (!newIP.isEmpty()) {
            Settings.setIpAddr(newIP);
            Settings.saveSettings();
            System.out.println("IP address updated to: " + newIP);
            System.out.println("Reconnecting...");
            NetworkManager.getInstance().disconnect();
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                // Ignore
            }
            // Attempt to connect to the new IP in TUI mode as GUI does
            boolean connected = NetworkManager.getInstance().connect(newIP);
            synchronized (notificationLock) {
                if (connected) {
                    lastNotification = "[INFO] Connected to " + newIP;
                } else {
                    lastNotification = "[WARNING] Failed to connect to " + newIP;
                }
            }
        }
    }

    private void reconnect() {
        System.out.println("\n--- Reconnecting ---");
        NetworkManager.getInstance().disconnect();
        try {
            Thread.sleep(500);
        } catch (InterruptedException e) {
            // Ignore
        }
        System.out.println("Attempting to reconnect to " + Settings.getIpAddr() + "...");
        // Attempt TCP/UDP connection to the configured IP
        boolean connected = NetworkManager.getInstance().connect(Settings.getIpAddr());
        synchronized (notificationLock) {
            if (connected) {
                lastNotification = "[INFO] Connected to " + Settings.getIpAddr();
            } else {
                lastNotification = "[WARNING] Failed to connect to " + Settings.getIpAddr();
            }
        }
    }

    private void toggleAutoScan() {
        Settings.SCAN_AUTOMATICALLY_FOR_CONTROLLERS = !Settings.SCAN_AUTOMATICALLY_FOR_CONTROLLERS;
        Settings.saveSettings();
        System.out.println("\nAuto-scan controllers: " + 
            (Settings.SCAN_AUTOMATICALLY_FOR_CONTROLLERS ? "ENABLED" : "DISABLED"));
        
        if (!Settings.SCAN_AUTOMATICALLY_FOR_CONTROLLERS) {
            System.out.println("Note: You can still manually scan by selecting option 1.");
        }
    }

    private void quit() {
        System.out.println("\nShutting down...");
        running = false;
        scanner.close();
        System.exit(0);
    }

    @Override
    public void showMessageBox(MessageBox msg) {
        if (msg == null || msg.getMessage() == null) {
            log.info("Can't show the message box");
            return;
        }
        String prefix = "";
        switch (msg.getType()) {
            case MessageBox.MESSAGE_ERROR:
                prefix = "[ERROR] ";
                break;
            case MessageBox.MESSAGE_WARNING:
                prefix = "[WARNING] ";
                break;
            case MessageBox.MESSAGE_INFO:
                prefix = "[INFO] ";
                break;
        }
        synchronized (notificationLock) {
            lastNotification = prefix + msg.getMessage();
        }
    }
}
