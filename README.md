To build the project:
    $ mvn package

On Mac OS and Linux you can start a web server that will serve the REST resources like so:
    $ java -cp target/classes:target/dependency/* net.betaengine.avrdude.heroku.Main

On Windows you'll need to modify the classpath portion to match the Windows style.

Note: this is the same command as you find in the file Procfile.

Now you can query all or parts of the avrdude.conf file as JSON.

E.g. to retrieve the JSON details for the part with signature 0x1e9514:
    $ curl -H "Accept: application/json" http://localhost:8080/conf/parts/signatures/0x1e9514

To see the same resource as nicely rendered HTML open the same URL in your browser.

At the bottom of every such HTML page is a summary of all the available resources.
