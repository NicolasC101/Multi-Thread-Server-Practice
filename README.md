# Multi-Thread-Server-Practice
A simple multithread server practice. Modified to use threadpool.


# Web Server in Java

This project is a simple multithreaded web server implemented in Java.

## Prerequisites

- Java Development Kit (JDK) 8 or later
- A terminal or command prompt

## Compilation

To compile the server, open a terminal in the project directory and run:

```sh
javac ServidorWeb.java
```

## Execution

To start the server, execute:

```sh
java ServidorWeb
```

The server will start listening on port `6789` for incoming HTTP requests.

## Testing

Once the server is running, you can test it using a web browser or `curl`:

```sh
curl http://localhost:6789
```

You should see the HTTP request logged in the console.

Alternatively, you can open a web browser and enter your local IP address followed by `:6789`, for example:

```
http://192.168.1.100:6789
```

You can also request specific files by entering:

```
http://localhost:6789/index.html
http://localhost:6789/silence.jpg
```

This will allow you to test different file types served by the server.

## Stopping the Server

To stop the server, press `CTRL + C` in the terminal.


