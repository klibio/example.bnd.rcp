# Debian docker image with browser based UI

 [How To Remotely Access GUI Applications Using Docker and Caddy on Debian 9](https://www.digitalocean.com/community/tutorials/how-to-remotely-access-gui-applications-using-docker-and-caddy-on-debian-9) guide which is used to transmit the display of a network machine to the local machine.

The main container is based on the Debian OS, for which lightweight images are available. For the configuration of the X11 server and supervisor please refer to the guide mentioned above.

Lastly, Java is added. From our test image the applications can be started by right clicking the black background box. This will open a context menu that includes all of the example projects and some debugging tools.
