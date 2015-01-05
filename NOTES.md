Local Heroku Setup
------------------

I did all the initial Heroku setup (installed toolbelt etc.) as per Heroku's "[Getting Started with Java](https://devcenter.heroku.com/articles/getting-started-with-java)".

Deploying on Heroku
--------------------

```bash
$ heroku create avrdude-conf
$ git push heroku master
$ heroku info
```

That's it - now it's available at http://avrdude-conf.herokuapp.com/conf

Luckily the subdomain `avrdude-conf` was (perhaps unsurprisingly) available.

For more generic names you can omit the name part and just do:

```bash
$ heroku create
```

Heroku will then create a random name, e.g. immense-inlet-4196.

If you have your own domain for which you can create CNAME records you can create a subdomain with the your desired name and point it at the Heroku created name as outlined on Heroku's "[Custom Domains](https://devcenter.heroku.com/articles/custom-domains)" page.

Via the Heroku web interface in the settings for the newly created app one can optionally link the application with a GitHub repo.

Heroku CLI and REST API
-----------------------

The Heroku CLI tools are documented to some extent on their "[Using the CLI](https://devcenter.heroku.com/articles/using-the-cli)" page.

But as you'll get more help from:

```bash
$ heroku help
```

The tools are just a frondend to Heroku's [REST based API](https://devcenter.heroku.com/articles/platform-api-reference).

Postgres DB
-----------

By default each app automatically gets a dev tier postgres DB.

You can get the postgres version like so:

```bash
$ heroku pg:info
```

This is useful when looking up documentation.

This and much more, along with interacting via Java is covered on the [Heroku PostgreSQL page](https://devcenter.heroku.com/articles/heroku-postgresql).

You need the need the psql client installed locally before you can create tables on Heroku etc. via the CLI:

```bash
$ sudo apt-get install postgresql-client
```

Now you can create a table like so:

    $ heroku pg:psql
    => CREATE TABLE cache (
    >    uuid char(36) NOT NULL,
    >    cachekey varchar(2048) NOT NULL,
    >    cachevalue text NOT NULL
    > ); -- Note: KEY and VALUE are SQL keywords.
    => \dt
    => \q

`\dt` is psql's rather obscure `show tables` command.
`\q` is the quit command.

When running in the Heroku environment Heroku automatically defines the environment variable `DATABASE_URL`, to see its value do:

```bash
$ heroku config
```

To connect from outside the Heroku infrastructure, e.g. when doing local testing, you need to set `DATABASE_URL` yourself to the same value with the following appended:

    &ssl=true&sslfactory=org.postgresql.ssl.NonValidatingFactory

See the "[Connecting to a databases remotely](https://devcenter.heroku.com/articles/connecting-to-relational-databases-on-heroku-with-java#connecting-to-a-database-remotely)" section of Heroku's page on connecting to the Postgres DB from Java.

Setting additional environment variables
----------------------------------------

Heroku defines a number of environment variables, such as `DATABASE_URL`, you can define additional ones like so:

```bash
$ heroku config:set MY_VAR=my_value
```

Creating initial POM and project structure
--------------------------------------

There are a number of Maven archetypes that can be used to create an initial POM and project layout for a Heroku targeted project.

In their article on deploying a Java web application using the Jetty `webapp-runner` the Heroku team describe [using the standard `maven-archetype-webapp`](https://devcenter.heroku.com/articles/deploy-a-java-web-application-that-launches-with-jetty-runner#create-an-application-if-you-don-t-already-have-one).

```bash
$ mvn archetype:generate \
    -DarchetypeArtifactId=maven-archetype-webapp \
    -DgroupId=my.package.name -DartifactId=my-artifact-name \
    -DinteractiveMode=false
```

Note: they reference a very old mortbay version of `jetty-runner`, you can find the latest Eclipse version by searching for `g:"org.eclipse.jetty" AND a:"jetty-runner"` on [search.maven.org](http://search.maven.org/).

The Jersey project maintains an archetype for Heroku that creates a simple roll-your-own alternative to `jetty-runner`:

```bash
$ mvn archetype:generate \
    -DarchetypeGroupId=org.glassfish.jersey.archetypes -DarchetypeArtifactId=jersey-heroku-webapp \
    -DgroupId=my.package.name -DartifactId=my-artifact-name \
    -DinteractiveMode=false
```

It also generates the initial Heroku `Procfile`, `system.properties`, `web.xml` and some Java classes to get started with.  
See the [Heroku web application](https://jersey.java.net/documentation/latest/getting-started.html#heroku-webapp) section of the Jersey documentation.

The Codehaus Jetty group maintain an archetype that creates projects that bundle up nicely for Heroku (as [described by Samuel Sharaf](http://samuelsharaf.wordpress.com/2011/11/06/create-a-simple-java-web-app-using-maven-and-upload-to-heroku/)):

```bash
$ mvn archetype:generate \
    -DarchetypeGroupId=org.mortbay.jetty.archetype -DarchetypeArtifactId=jetty-archetype-assembler \
    -DarchetypeVersion=8.1.16.v20140903 \
    -DgroupId=my.package.name -DartifactId=my-artifact-name \
    -DinteractiveMode=false
```

Heroku's own [quick start documentation](https://devcenter.heroku.com/articles/getting-started-with-java#prepare-the-app) doesn't bother with an archetype or `jetty-runner` instead they use [a canned POM](https://github.com/heroku/java-getting-started/blob/master/pom.xml) and like Jersey their own [super basic runner class](https://github.com/heroku/java-getting-started/blob/master/src/main/java/Main.java) (see the `main` method, the rest of the class is actually a servlet).

*Important:* everything gets out of date fast. All the above articles, guides and archetypes generally reference out of date versions of Jetty and pull in old dependencies, e.g. Glassfish JSP support despite Jetty having moved to an Apache implementation. So it's best to look at the Maven sections in the [current Jetty documentation](http://www.eclipse.org/jetty/documentation/current/), in particular "[Developing a standard web application with Jetty and Maven](http://www.eclipse.org/jetty/documentation/current/maven-and-jetty.html#developing-standard-webapp-with-jetty-and-maven)".

Miscellaneous
-------------

Change the simple caching strategy to use the DB:

```bash
$ heroku config:set BETAENGINE_CACHE=net.betaengine.util.cache.DbCache
```

Using Jackson 2.X with Jersey is covered by Jersey JIRA 2322 and 2335.

The tests committed for these JIRAs demonstrate how to set things up (there's no other obvious documentation).

To find them:

    $ git clone git@github.com:jersey/jersey.git
    $ cd jersey
    $ git log
    Search for 2335 and then look at the relevant commit:
    $ git show 669c14c19eef15e35ba82803889adc3e893200bb
