
# HIDtoVPADNetworkClient
[![Build Status](https://circleci.com/gh/QuarkTheAwesome/HIDtoVPADNetworkClient.svg?style=shield&circle-token=:circle-token)](https://circleci.com/gh/QuarkTheAwesome/HIDtoVPADNetworkClient)
---
[Current Stable Release](https://github.com/QuarkTheAwesome/HIDtoVPADNetworkClient/releases/latest) | [Nightly builds](https://github.com/QuarkTheAwesome/HIDtoVPADNetworkClient/releases) | [Issue Tracker](https://github.com/QuarkTheAwesome/HIDtoVPADNetworkClient/issues) | [Support Thread](http://gbatemp.net/threads/hid-to-vpad-network-client.466150/)

A Java application to send data from various controllers to [HID to VPAD](https://github.com/Maschell/hid_to_vpad); a homebrew application for the Wii U.

Using this client, you can use unsuported devices such as XInput and Bluetooth controllers with HID to VPAD!

Please check the releases page for the latest feature list.

Configuration files for HID to VPAD can be found [here](https://github.com/Maschell/controller_patcher_configs).

## Running the Application

### GUI Mode (Default)
Simply run the JAR file:
```bash
java -jar HIDToVPADNetworkClient.jar
```

### TUI Mode (Text User Interface / No GUI)
For headless systems, SSH sessions, or if you prefer a command-line interface:
```bash
java -jar HIDToVPADNetworkClient.jar --tui
```

New command-line options useful for TUI/headless use:

- `--auto-connect`, `--autoconnect`, `-a`: attempt to connect to the configured Wii U IP on startup and wait for the connect result before showing the TUI menu. The menu header will reflect the actual connection state.
- `--debug`, `--tui-debug`, `-d`: enable TUI debug mode. When enabled the TUI will show informational device add/remove notifications and more verbose logging; otherwise the TUI keeps the terminal clean.

See [TUI_USAGE.md](TUI_USAGE.md) for detailed information about using the text-based interface.

Headless (no-menu) mode
-----------------------
If you want the client to run without showing the interactive menu (good for daemons, systemd services or SSH sessions where no input is available), use the `--headless` flag together with `--tui`:

```bash
java -jar HIDToVPADNetworkClient.jar --tui --headless
```

Common combinations:

- Auto-connect and run headless:
```bash
java -jar HIDToVPADNetworkClient.jar --tui --headless --auto-connect
```
- Headless with debug messages:
```bash
java -jar HIDToVPADNetworkClient.jar --tui --headless --debug
```

Systemd / service notes:
- When running as a systemd service run the JAR with `--tui --headless` so it doesn't expect interactive input.
- The included `nixos/hidtovpad-module.nix` (if you're on NixOS) can be adjusted to include `--headless` in `ExecStart`.


## Used Libraries
Lombok - https://projectlombok.org/index.html  
purejavahidapi - https://github.com/nyholku/purejavahidapi  
hid4java - https://github.com/gary-rowe/hid4java  
JXInput - https://github.com/StrikerX3/JXInput  
