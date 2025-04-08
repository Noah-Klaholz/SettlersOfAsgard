import subprocess
import time
import os
import signal
import sys
import platform

# Configuration
PORT = 9999
SERVER_HOST = "localhost"
CLIENT_NAMES = ["Thor", "Odin", "Loki", "Freya"]
JAR_PATH = None  # Will try to find automatically

def find_jar_file():
            """Try to find the game JAR file in common locations, then search recursively"""
            # First check common locations for quick access
            possible_locations = [
                "./build/libs/settlersOfAsgard.jar",
                "./settlersOfAsgard.jar"
            ]

            for loc in possible_locations:
                if os.path.exists(loc):
                    return os.path.abspath(loc)

            # If not found in common locations, search recursively
            print("Searching for settlersOfAsgard.jar in current directory and subdirectories...")
            for root, dirs, files in os.walk('.'):
                for file in files:
                    if file == "settlersOfAsgard.jar":
                        jar_path = os.path.join(root, file)
                        print(f"Found JAR at: {jar_path}")
                        return os.path.abspath(jar_path)

            # If still not found, ask the user
            user_path = input("JAR file not found. Enter the path to the game JAR file: ")
            if os.path.exists(user_path):
                return os.path.abspath(user_path)
            else:
                print(f"JAR file not found at {user_path}")
                sys.exit(1)

def run_server_and_clients():
    """Main function to run server and clients"""
    global JAR_PATH
    JAR_PATH = find_jar_file()
    print(f"Using JAR file: {JAR_PATH}")

    processes = []

    try:
        # Start server
        server_cmd = f"java -jar {JAR_PATH} server {PORT}"
        print(f"Starting server: {server_cmd}")
        server_process = subprocess.Popen(server_cmd, shell=True)
        processes.append(server_process)

        # Wait for server to start up
        print("Waiting for server to start...")
        time.sleep(3)

        # Start clients
        for name in CLIENT_NAMES:
            client_cmd = f"java -jar {JAR_PATH} client {SERVER_HOST}:{PORT} {name}"
            print(f"Starting client with name {name}: {client_cmd}")
            client_process = subprocess.Popen(client_cmd, shell=True)
            processes.append(client_process)
            # Brief pause between client launches
            time.sleep(1)

        print("All processes started. Press Ctrl+C to stop.")

        # Keep the script running until Ctrl+C
        while True:
            time.sleep(1)

    except KeyboardInterrupt:
        print("\nShutting down processes...")

    finally:
        # Clean up processes
        for process in processes:
            try:
                if platform.system() == "Windows":
                    process.terminate()
                else:
                    process.send_signal(signal.SIGTERM)
            except:
                pass

        print("All processes terminated.")

if __name__ == "__main__":
    run_server_and_clients()