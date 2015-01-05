EmbeddedHttpServer
==================

Simple HTTP server written in Java and packaged within just one JAR file.

Features
--------

- Multiple parallel connections
- Persistent connections
- Range requests
- Directory indexes
- Basic access authentication
- Support for If-Modified-Since requests

Basic Usage
-----------

Running the server is very easy. The bare minimum is:

```java
HttpServer httpServer = new HttpServer.Builder().build();
httpServer.startServer();
```

This starts up with the defaults, most importantly using `/tmp` as the page root and `10001` as the port to listen on.

As you might expect, stopping the server is:

```java
httpServer.stopServer();
```