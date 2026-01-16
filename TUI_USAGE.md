# TUI Mode - Text User Interface

The HID To VPAD Network Client now supports running in TUI (Text User Interface) mode without requiring a graphical interface. This is useful for headless systems, SSH sessions, or situations where you prefer a command-line interface.

## Running in TUI Mode

To start the application in TUI mode, use one of the following command-line flags:

```bash
java -jar HIDToVPADNetworkClient.jar --tui
```

Or alternatively:
```bash
java -jar HIDToVPADNetworkClient.jar --no-gui
java -jar HIDToVPADNetworkClient.jar -t
```

## TUI Menu Options

Once running in TUI mode, you'll see a menu with the following options:

### 1. List Controllers
Displays all attached controllers with their current status (ACTIVE/INACTIVE), identifier, and type.

### 2. Activate Controller
Shows a list of all attached controllers and allows you to select one to activate. Activated controllers will send their input to the Wii U.

### 3. Deactivate Controller
Shows a list of active controllers and allows you to deactivate one or all of them.

### 4. Show Status
Displays the current system status including:
- IP address of the Wii U
- Connection status
- Auto-scan setting
- Auto-activate setting
- Number of attached/active controllers

### 5. Change IP Address
Allows you to change the IP address of the Wii U to connect to. The new setting is saved automatically.

### 6. Reconnect
Forces a reconnection attempt to the Wii U.

### 7. Toggle Auto-scan
Enables or disables automatic controller scanning. When disabled, controller detection only happens when you manually list controllers.

### 8. Quit
Exits the application.

## Configuration

The TUI mode uses the same configuration file as the GUI version (`config/hidtovpad.properties`). You can edit this file manually or use the TUI menu options to change settings.

## Help

To see command-line options:
```bash
java -jar HIDToVPADNetworkClient.jar --help
```

## Command-line flags (TUI-specific)

- `--auto-connect`, `--autoconnect`, `-a`
   - When supplied the application will attempt to connect to the IP configured in `config/hidtovpad.properties` before showing the TUI menu and continuously thereafter every 30 seconds.

- `--debug`, `--tui-debug`, `-d`
   - Enables TUI debug mode. When debug is enabled you will see informational device add/remove messages and additional logging in the TUI. When disabled, the TUI keeps the terminal clean and only shows warnings/errors and the TUI notifications.

Notes:
- Device add/remove notifications are shown only in TUI debug mode to avoid cluttering the menu during normal operation.

## Tips

1. **Auto-activate Controllers**: Set `autoActivatingController=true` in the config file to automatically activate controllers when they're detected.

2. **Auto-scan**: Keep `scanAutomaticallyForControllers=true` enabled to continuously detect new controllers.

3. **Pre-configure IP**: Edit `config/hidtovpad.properties` and set the correct IP address before running to avoid manual configuration.
