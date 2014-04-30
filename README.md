On Mac OS and Linux you can start a web server that will serve the REST resources like so:
$ source Procfile

On Windows you'll need to run the command contained in Procfile with the classpath modified appropriately for Windows.

Now you can query all or parts of the avrdude.conf file as JSON.

E.g. to retrieve the JSON details for the part with signature 0x1e9514:
$ curl -H "Accept: application/json" http://localhost:8080/conf/parts/signatures/0x1e9514

To see the same resource as nicely rendered HTML open the same URL in your browser.

At the bottom of every such HTML page is a summary of all the available resources.
