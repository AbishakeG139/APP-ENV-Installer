# APP-ENV-Installer
This app helps set up the environment, install the required applications, configure environment properties, and start the applications in windows.


# Inno Setup uses its own scripting language based on Pascal
Inno Setup can generate a Windows installer (.exe) that runs on any Windows system without requiring a pre-installed JDK. By including custom script logic, the installer can automatically download and install JDK 17, set the environment path, and then run the JAR file.

# java
This Java project provides an automated environment setup using an Inno Setup installer (APP-ENV-Installer.exe)

Installation Workflow

System Architecture Detection
The installer automatically detects whether the operating system is 64-bit or 32-bit Windows.

Activation Key Validation
The user is prompted to enter an activation key, which is validated before proceeding with the installation.

Environment Preparation

Creates necessary directories for:

Downloads
Backups
Logs
Configuration and storage
Sets the system environment variable:
ABI_DEV_ENV=<installation_path>


Component Installation

The installer automatically downloads and installs the following components in sequence:
Node.js
MySQL
Apache Tomcat
VCTWorker (a Node.js-based service)

Configuration and Service Setup

Automatically configures environment paths and variables.
Registers Tomcat and VCTWorker as Windows services for automatic startup.
Logs all actions for future reference and troubleshooting.