# IF2211_TK1_18222130

## Description
This program is a solver for IQ Puzzler Pro puzzles using Brute Force and Backtracking algorithm. It was developed as part of the IF2211 Algorithm Strategies course assignment (Tugas Kecil 1). The program provides a graphical user interface to load puzzle configurations, solve them, and visualize the solutions in 3D.

## Features
- 3D visualization of puzzle solutions
- Interactive camera controls (rotate, zoom)
- Solution saving in both text and image formats
- Progress indicator during solving
- Detailed statistics (solving time, iterations)

## Requirements
1. Java Development Kit (JDK) 17 or higher
2. JavaFX SDK 17 or higher
4. VcXsrv (for WSL users)

## Installation

### Windows
1. Install JDK 23:
   - Download from [Oracle](https://www.oracle.com/java/technologies/downloads/)
   - Run installer
   - Set environment variables:
   ```bash
   setx JAVA_HOME "C:\Program Files\Java\jdk-23"
   setx PATH "%PATH%;%JAVA_HOME%\bin"

2. Install JavaFX 23:
   - Download from [Gluon](https://gluonhq.com/products/javafx/)
   - Extract to preferred location
   - Set environment variable
   ```bash
   setx PATH_TO_FX "C:\Program Files\Java\javafx-sdk-23\lib"

### Linux/WSL
1. Install X Server (WSL only):
   - Download [VcXsrv](https://sourceforge.net/projects/vcxsrv/)
   - Configure XLaunch:
      - Multiple windows
      - Display number: 0
      - Start no client
      - ✓ Disable access control

2. Install JDK 23:
   ```bash
   wget https://download.oracle.com/java/23/latest/jdk-23_linux-x64_bin.tar.gz
   sudo tar zxvf jdk-23_linux-x64_bin.tar.gz -C /usr/lib/jvm/

3. Install JavaFX 23
   ```bash
   wget https://download2.gluonhq.com/openjfx/23/openjfx-23_linux-x64_bin-sdk.zip
   sudo unzip openjfx-23_linux-x64_bin-sdk.zip -d /usr/lib/jvm/

4. Configure environment
   ```bash
   echo 'export JAVA_HOME=/usr/lib/jvm/jdk-23' >> ~/.bashrc
   echo 'export PATH=$JAVA_HOME/bin:$PATH' >> ~/.bashrc
   echo 'export PATH_TO_FX=/usr/lib/jvm/javafx-sdk-23/lib' >> ~/.bashrc
   echo 'export DISPLAY=:0' >> ~/.bashrc
   echo 'export LIBGL_ALWAYS_INDIRECT=1' >> ~/.bashrc
   source ~/.bashrc

## Bulding and Running

### Windows
1. Compile
   ```bash
   javac -d bin --module-path "%PATH_TO_FX%" --add-modules javafx.controls,javafx.graphics,javafx.base,javafx.swing src/module-info.java src/Main.java src/puzzle/*.java src/file/*.java
2. Run
   ```bash
   java --module-path "%PATH_TO_FX%" --add-modules javafx.controls,javafx.graphics,javafx.base,javafx.swing -cp bin main.Main

### Linux/WSL
1. Start X Server (WSL only)
   - Launch XLaunch with saved configuration
2. Compile
   ```bash
   javac -d bin --module-path $PATH_TO_FX --add-modules javafx.controls,javafx.graphics,javafx.base,javafx.swing src/module-info.java src/Main.java src/puzzle/*.java src/file/*.java
3. Run
   ```bash
   java --module-path $PATH_TO_FX --add-modules javafx.controls,javafx.graphics,javafx.base,javafx.swing -Dprism.order=sw -cp bin main.Main

## Input Format
The program accepts text files with the following format:
1. First line: puzzle dimensions (rows columns)
2. Second line: puzzle type (DEFAULT/CUSTOM)
3. Following lines: puzzle blocks configuration

Example input file:
```
5 5 7
DEFAULT
A
AA
B
BB
C
CC
D
DD
EE
EE
E
FF
FF
F
GGG
```

## Author
Bryan P. Hutagalung (18222130)  
Information System and Techhnology 
Bandung Institute of Technology
2024

## Course Information
IF2211 Algorithm Strategies  
Informatics Engineering
School of Electrical Engineering and Informatics  
Bandung Institute of Technology