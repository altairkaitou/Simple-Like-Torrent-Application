# SimpleTorrent

SimpleTorrent is a Java-based simulation of a torrent-like file sharing system designed for educational purposes in a Computer Networks course. The system utilizes a centralized tracker to manage peer connections and file metadata, enabling peers to share and request files efficiently.

## Table of Contents
- [Features](#features)
- [Technologies Used](#technologies-used)
- [Installation](#installation)
- [Usage](#usage)
- [Components](#components)
- [Known Limitations](#known-limitations)
- [Future Work](#future-work)

## Features
- **Centralized Tracker**: Manages peer information and file metadata.
- **File Sharing**: Enables peers to share and request files without direct file transfer to the tracker.
- **Concurrent Downloads**: Supports downloading files from multiple peers simultaneously.
- **File Piece Management**: Handles files in pieces, enabling efficient data transfer.
- **Checksum Validation**(Incompleted): Ensures data integrity through SHA-256 checksums for each piece of the file.

## Technologies Used
- **Java**: Core programming language.
- **TCP/IP Sockets**: Used for network communications.
- **Multithreading**: To handle concurrent processes.

## Installation
To set up the SimpleTorrent system, follow these steps:
1. **Clone the repository**:
    ```bash
    git clone https://github.com/altairkaitou/simpletorrent---Computer-Network.git
    ```
2. **Navigate to the project directory**:
    ```bash
    cd simpletorrent
    ```
3. **Compile the Java files**:
    ```bash
    javac TrackerServer.java Peer.java FileUtilities.java FilePieceTracker.java TorrentCLI.java
    ```

## Usage

### Starting the Tracker Server
To start the Tracker Server, run:
```bash
java TrackerServer
```
To run a Peer Client, execute:
```bash
java Peer
```
command1: share + File Path 
command2: request + Host + Port + File Name
command3: exit

### Using UI
To start the program, run:
```bash
java TorrentCLI
```
Follow the on-screen instructions to share or request files.
