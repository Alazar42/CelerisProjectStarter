# Celeris Project Creator

Celeris Project Creator is a Kotlin-based desktop application that simplifies the process of creating new projects using the Celeris backend framework. This application is designed to help developers quickly set up and start building their backend applications with minimal effort.

## Table of Contents

- [Features](#features)
- [Installation](#installation)
- [Usage](#usage)
- [Building the Application](#building-the-application)
- [Contributing](#contributing)
- [License](#license)

## Features

- **Quick Project Creation**: Instantly create new Celeris projects with pre-configured settings.
- **Cross-Platform**: Supports both Linux (.deb) and Windows (.exe) installations.
- **User-Friendly Interface**: A modern interface built with Jetpack Compose for an intuitive user experience.
- **Open Source**: Fully open-source for community contributions and customization.

## Installation

### For Linux Users

1. Download the `.deb` file from the [Releases](https://github.com/Alazar42/CelerisProjectStarter/releases) page.
2. Open a terminal and navigate to the directory where the file is located.
3. Install the package using the following command:

   ```bash
   sudo dpkg -i celeris-project-creator.deb
   ```

4. If there are any dependency issues, run:

   ```bash
   sudo apt-get install -f
   ```

### For Windows Users

The Windows installer will be available soon! Please check back later for updates.

## Usage

1. Launch the **Celeris Project Creator** application from your applications menu.
2. Upon starting, you’ll be greeted with a simple interface where you can create a new Celeris project.
3. Enter your project details, including the project name and location.
4. Click **Create Project** to generate a new Celeris project in your specified directory.
5. Navigate to the created project folder and follow the documentation within the project to get started with development.

## Building the Application

To build the Celeris Project Creator from source:

1. Clone the repository:

   ```bash
   git clone https://github.com/Alazar42/CelerisProjectStarter.git
   ```

2. Navigate to the project directory:

   ```bash
   cd CelerisProjectStarter
   ```

3. Build the project using Gradle:

   ```bash
   ./gradlew build
   ```

4. You can create native distributions (such as `.deb` for Linux and `.exe` for Windows) by running:

   ```bash
   ./gradlew package
   ```

5. The built files will be located in the `build/distributions` directory.

## Contributing

Contributions are welcome! If you have suggestions for improvements or new features, feel free to open an issue or submit a pull request.

## License

This project is licensed under the MIT License. See the [LICENSE](LICENSE) file for more details.
