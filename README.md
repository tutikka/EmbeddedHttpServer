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

Configuration
-------------

The snippet below shows an example using all of the available configuration options.

```java
HttpServer httpServer = new HttpServer.Builder()
	.setPageRoot("/tmp")
	.setPortNumber(10001)
	.setMaxParallelConnections(10)
	.setAllowDirectoryIndexes(true)
	.setBasicAccessAuthentication(new BasicAccessAuthentication("realm", "username", "password"))
	.addWelcomeFile("index.html")
	.addContentType("mp4", "video/mp4")
	.build();
```

Interactive Shell
-----------------

The package includes an interactive stand-alone shell, which can be used to test the server for example from a console.

Running the shell:

```
$ java -cp embedded-http-server-0.0.1.jar com/tt/embeddedhttpserver/HttpServer
-----------------------------
| Embedded Http Server Shell |
------------------------------
| start |   Start the server |
| stop  |    Stop the server |
| help  |    Print this help |
| quit  |         Exit shell |
------------------------------
[INFO   ] 06.01.15 09:11:37 config: pageRoot = /tmp
[INFO   ] 06.01.15 09:11:37 config: portNumber = 10001
[INFO   ] 06.01.15 09:11:37 config: maxParallelConnections = 10
[INFO   ] 06.01.15 09:11:37 config: allowDirectoryIndexes = false
[INFO   ] 06.01.15 09:11:37 config: contentTypes = {css=text/css, log=text/plain, gif=image/gif, js=text/javascript, mid=audio/mid, eot=application/vnd.ms-fontobject, ico=image/x-icon, mov=video/quicktime, xml=text/xml, jpeg=image/jpeg, html=text/html, htm=text/html, otf=application/x-font-opentype, jpg=image/jpeg, qt=video/quicktime, svg=image/svg+xml, mpg=video/mpeg, ttf=application/x-font-ttf, png=image/png, wav=audio/x-wav, woff=application/font-woff, mp4=video/mp4, txt=text/plain, mp3=audio/mpeg, mpeg=video/mpeg}
[INFO   ] 06.01.15 09:11:37 config: welcomeFiles = [index.html, index.htm]
[INFO   ] 06.01.15 09:11:37 config: basicAccessAuthentication = null
```

Starting the server:

```
start
[INFO   ] 06.01.15 09:15:36 accepting connections on port 10001
[INFO   ] 06.01.15 09:15:36 client executor service created
```

Stopping the server:

```
stop
[INFO   ] 06.01.15 09:16:00 closed server socket
[INFO   ] 06.01.15 09:16:00 client executor service shut down
```

Exiting the shell:

```
quit
```