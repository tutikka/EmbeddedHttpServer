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
- Support for tiny url style path aliases

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
	.addAlias("/abc", "/index.html")
	.setAllowOnlyAliases(true)
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
[INFO   ] 08.04.15 20:16:18 config: pageRoot = /tmp
[INFO   ] 08.04.15 20:16:18 config: portNumber = 10001
[INFO   ] 08.04.15 20:16:18 config: maxParallelConnections = 10
[INFO   ] 08.04.15 20:16:18 config: allowDirectoryIndexes = true
[INFO   ] 08.04.15 20:16:18 config: contentTypes = {css=text/css, log=text/plain, gif=image/gif, js=text/javascript, mid=audio/mid, eot=application/vnd.ms-fontobject, ico=image/x-icon, mov=video/quicktime, xml=text/xml, jpeg=image/jpeg, html=text/html, htm=text/html, otf=application/x-font-opentype, jpg=image/jpeg, qt=video/quicktime, svg=image/svg+xml, mpg=video/mpeg, ttf=application/x-font-ttf, png=image/png, wav=audio/x-wav, woff=application/font-woff, mp4=video/mp4, txt=text/plain, mp3=audio/mpeg, mpeg=video/mpeg}
[INFO   ] 08.04.15 20:16:18 config: welcomeFiles = [index.html, index.htm]
[INFO   ] 08.04.15 20:16:18 config: basicAccessAuthentication = { realm = realm, username = username, password = ** }
[INFO   ] 08.04.15 20:16:18 config: aliases = {/abc=/index.html}
[INFO   ] 08.04.15 20:16:18 config: allowOnlyAliases = true
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

Tiny URLs
---------

It is possible to define short aliases for longer urls, for example:

```
httpServer.addAlias("/abc", "/some/other/longer/path/awesome.html");
```

This makes the server translate the request path before other processing, so it is not a redirect for example. Note also that the alias is a key in a map, so adding one that already exists effectively overwrites the previous.

You can also choose to allow only aliases to be processed:

```
httpServer.setAllowOnlyAliases(true);
```

Now, if the alias is not found, a ``HTTP 404`` will be returned.