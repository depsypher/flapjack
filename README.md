# Flapjack
A tasty java stack for building modern web applications.

## What is Flapjack?
Flapjack is a java webapp that comes with functionality virtually any site is going to need.  
Use it as a starting point for building your own web applications.

## Why use Flapjack?
Flapjack brings together a bunch of good tech all nicely integrated together and ready to use.

* Jersey: for REST, JAX-RS style
* Guice: for injecting dependencies
* Postgres: for databasing
* Hibernate: for ORMing
* Ehcache: for caching
* Metrics: for troubleshooting
* Jackson: for JSONing
* Cambridge Templates: for templating

The functionality that you get out of the box from Flapjack includes:

* User account creation, login, and session management.
* Integration with [Mozilla Persona](http://www.mozilla.org/en-US/persona/) for login.
* Database caching.
* Cool server metrics.
* And much more!

## How do I get up and running?
Flapjack doesn't mandate any particular setup per-se, but what follows is likely the path of least resistance to getting started:

1. Put JDK7 or better on your machine
2. Install [Postgress.app](http://postgresapp.com/)
3. Install Eclipse, and the [Run Jetty Run](https://code.google.com/p/run-jetty-run/) plugin

Then:

`$ git clone https://github.com/depsypher/flapjack` (and import as existing project in Eclipse)  
`$ cd flapjack`  
`$ ./extras/setup.sh`

Then:

1. Right-click flapjack.launch in Eclipse and choose Run or Debug
2. Go here for the app: [http://localhost:8080/flapjack/](http://localhost:8080/flapjack/)
3. Go here for metrics: [http://localhost:8080/flapjack/admin/metrics?pretty=true](http://localhost:8080/flapjack/admin/metrics?pretty=true)
