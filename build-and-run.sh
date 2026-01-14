#!/bin/bash
# Script to build and run HIDToVPADNetworkClient in TUI mode

echo "Building HIDToVPADNetworkClient..."

# Check if Maven is installed
if ! command -v mvn &> /dev/null; then
    echo "Error: Maven is not installed. Please install Maven first."
    exit 1
fi

# Build the project
mvn -X clean package

# Check if build was successful
if [ $? -ne 0 ]; then
    echo "Build failed!"
    exit 1
fi

echo ""
echo "Build successful!"
echo ""
echo "To run in GUI mode:"
echo "  java -jar target/HIDToVPADNetworkClient-*.jar"
echo ""
echo "To run in TUI mode (no GUI):"
echo "  java -jar target/HIDToVPADNetworkClient-*.jar --tui"
echo ""
echo "For help:"
echo "  java -jar target/HIDToVPADNetworkClient-*.jar --help"
echo ""
