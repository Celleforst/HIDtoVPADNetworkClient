# TUI Quick Reference

## Starting the Application

```bash
# GUI Mode (default)
java -jar HIDToVPADNetworkClient.jar

# TUI Mode
java -jar HIDToVPADNetworkClient.jar --tui

# Help
java -jar HIDToVPADNetworkClient.jar --help
```

## TUI Menu Commands

| Option | Action                        |
|--------|-------------------------------|
| 1      | List Controllers              |
| 2      | Activate Controller           |
| 3      | Deactivate Controller         |
| 4      | Show Status                   |
| 5      | Change IP Address             |
| 6      | Reconnect                     |
| 7      | Toggle Auto-scan              |
| 8      | Quit                          |

## Configuration File

Location: `config/hidtovpad.properties`

```properties
# IP address of the Wii U
ipAddr=192.168.0.35

# Automatically activate newly detected controllers
autoActivatingController=true

# Continuously scan for new controllers
scanAutomaticallyForControllers=true

# Send data only when controller state changes
sendDataOnlyOnChanges=false

# Controller type filters
filterStates=[true, false, false, false, true, true]
```

## Quick Setup for Headless Operation

1. Edit config file with correct IP:
   ```bash
   nano config/hidtovpad.properties
   ```

2. Set auto-activation:
   ```properties
   autoActivatingController=true
   scanAutomaticallyForControllers=true
   ```

3. Run in background:
   ```bash
   nohup java -jar HIDToVPADNetworkClient.jar --tui > hidtovpad.log 2>&1 &
   ```

## Troubleshooting

### Controllers not detected
- Enable auto-scan (option 7)
- Manually list controllers (option 1)
- Check controller permissions
- Verify USB/Bluetooth connection

### Cannot connect to Wii U
- Check IP address (option 5)
- Verify Wii U is on same network
- Ensure HID to VPAD is running on Wii U
- Try reconnecting (option 6)

### Permission errors (Linux)
```bash
# Add user to input group
sudo usermod -a -G input $USER

# Log out and log back in for changes to take effect
```

## System Service Commands

```bash
# Start service
sudo systemctl start hidtovpad

# Stop service
sudo systemctl stop hidtovpad

# Restart service
sudo systemctl restart hidtovpad

# Check status
sudo systemctl status hidtovpad

# View logs
sudo journalctl -u hidtovpad -f

# Enable on boot
sudo systemctl enable hidtovpad

# Disable on boot
sudo systemctl disable hidtovpad
```
