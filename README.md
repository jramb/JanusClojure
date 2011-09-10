# JanusClojure

JanusClojure is a demo web application that uses Clojure on both the server and the cliend side (ClojureScript).
*If all you have is Clojure, everything looks... much clearer...*

The idea was to provide a minimal web application that contains many of the most basic tools and
even some tips and tricks for client-server communication for newstarters to get going. Maybe you find this
project useful as the starting point for your own application. JanusClojure just shows some techniques.

The server-side is designed to either being run on a local compojure instance or to be hosted on *Heroku*.

## Naming
This little project is named after the two-faced roman god [Janus](http://en.wikipedia.org/wiki/Janus_%28mythology%29).

Prerequisites:

1. I was working on Linux (Ubuntu in my case).
1. Java (for Clojure)
1. Leiningen
1. Optional: Heroku account and tools for actual hosting


## Bootstraping

Created a new leiningen project:

    $ lein new janusclojure
    Created new project in: /home/jorg/projects/janusclojure

If you want to host the app on Heroku (this is optional of course), create the Procfile:

    $ echo 'web: lein run -m janusclojure.core' >Procfile

Modify the `project.clj` file to something like this, add `:main` and some libraries:

    (defproject janusclojure "1.0.0-SNAPSHOT"
      :description "A 200% Clojure web applications"
      :dependencies [[org.clojure/clojure "1.2.1"]
                     [org.clojure/clojure-contrib "1.2.0"]
                     [ring/ring-core "0.3.11"]
                     [ring/ring-jetty-adapter "0.3.11"]
                     [compojure "0.6.5"]
                     ]
      :main janusclojure.core)

Now you can pull in the needed jars:

    $ lein deps

## Level 1: a very simple *Hello, world*
Now for the fun part, the code writing!
`lein new` left a simple file `src/janusclojure/core.clj` with only the `ns` part in it.
Lets fill it with some code, change it to the following: some requires for pulling in compojure and jetty.
Note that I use `:require` all the way. This is of course personal taste, but I like to know where functions
come from.

To start with we define only the `GET "/"` route which answers only to only just that. The `_`
after the `GET` indicates for now that we are not interested in the request object and we return
the static string `Hello, world`.

    (ns janusclojure.core
        (:require [ring.adapter.jetty :as jetty])
        (:require [compojure.core :as compojure])
        (:require [compojure.route :as route])
        )

    (compojure/defroutes app-routes
        (compojure/GET   "/" _ "Hello, world!")
        )

    (defn -main []
        (let [port (Integer/parseInt (get (System/getenv) "PORT" "8080"))]
            (jetty/run-jetty (var app-routes) {:port port})))

That's it, now we can test it. Fire up the Jetty instance by calling:

    $ lein run

This starts the jetty instance listening on port 8080 (unless overruled by a environment variable `PORT`).
To check the result, use a web browser and check `http://localhost:8080/`.

You should see just that, "Hello, world!".

## Level 2: let the server do something
Add the following function to the code:

    (defn gen-uuid
      "Returns a new UUID without the dashes. Take good care of it, it is unique!"
      []
      (apply str (re-seq #"[^-]" (str (java.util.UUID/randomUUID)))))

This function returns a UUID, a universally unique random number in a hex form. This has no special meaning, but
UUIDs can come very handy in client-server communication. The thing is that the chance of generating the same
number twice is subatomically little.

What we want to do here is to expose an `API` for the client to fetch those generated numbers.
The result should be served under the URL *"/uuid"*, so we add the following to our routes:

    (compojure/GET   "/uuid"  _   (str {:uuid (gen-uuid)}))

Note that we still return a string, but we stringify a map with only one key `:uuid` which has the value of
the fresh generated uuid. That means we talk Clojure to the client, yeah!

Abort the Jetty instance (`Ctrl-C` and then `lein run` again) and check for http:localhost:8080/uuid in your
browser. You should see something like this:

    {:uuid "f6627eaea076481b9e5ac674ed55a9d2"}

Reload a few times and the random uuid should change every time. (Be aware that there could some caching issues
if you use this in real life, to be sure add a questionmark to the URL, such as `/uuid?`)

Easy, eh?

## Level 3: static files (such as HTML, JavaScript and CSS)
Well you *can* generate HTML programmatically in Clojure. There are two negative things about this: performance
and maintainance. Alright, you may disagree in both, but producing server-active HTML is well documented on the
web if you are looking for that.
Here we use the static file approach instead, which also comes natural because you still have to serve JavaScript files,
CSS and maybe images. We fill the HTML with live using ClojureScript (that is JavaScript in the end).

Create a subdirectory under the `src` directory, named (for example) `html`:

    $ mkdir src/html

Create an file `src/html/index.html` with the following contents there.

    <!DOCTYPE HTML>
    <html>
      <head>
        <title>JanusClojure</title>
        <link rel="stylesheet" href="janusclojure.css">
      </head>
    <body>
      <h3>JanusClojure</h3>
      <table>
        <tr>
          <th><label for="your-name">Your name:</label></th>
          <td><input id="your-name"/></td>
          </tr>
          <tr>
              <th><label for="your-number">A number:</label></th>
              <td><input id="your-number"/></td>
          </tr>
          <tr>
              <th><label for="random-text">Some random text:</label></th>
              <td><input id="random-text"/>
                  <input type="button" id="getUUID" value="Fetch"/>
              </td>
          </tr>
          <tr>
              <th></th>
              <td><input type="button" id="sendBtn" value="Send it"/></td>
          </tr>
        </table>
    </body>
    </html>

Now, change the app-routes definition to the following so start serving static files.
Also add a little helper function for redirects:

    (defn redirect-to
      "Generates a redirect response"
      [loc]
      {:status 302
       :headers {"Location" loc}})

    (compojure/defroutes app-routes
        (compojure/GET   "/"      _   (redirect-to "/index.html"))
        (compojure/GET   "/hello" _   "Hello, world!")
        (compojure/GET   "/uuid"  _   (str {:uuid (gen-uuid)}))
        (route/resources "/" {:root "html"})
        (route/not-found "Page not found")
        )

Now what does that mean?

1. We moved our `"Hello, world!"` out of the way to another URL (`/hello`).
1. We added a `resource` line which will match everything (that has not been matched earlier)
  to a static resource which is located under the `"html"` directory (seen from `src`)
1. To be clear about the rest (not matched earlier, not found in the resources) we
  add a `not-found` declaration. This is not really necessary, but it gives you the chance to
  give a specific "not found page" instead of Jettys baked in 404.
1. The `"/"` GET now redirects to `"/index.html"`.

Short about the ring handler: the return value is either a string, a stream or a map with the
keys `:status` (the status code), `:headers` (a map of header data) and `:body` (the contents of
the result). Everything has defaults. More about that later.

Now you can restart the jetty instance again and try the (very static) HTML files, using http://localhost:8080/
which automatically redirects to http://localhost:8080/index.html which then again serves the not-yet functional
HTML form. Note that the style-sheet "janusclojure.css" is not really needed yet, but you could add one under `src/html`
as well. It is also possible to create subdirectories there as well, but in our case I left it alone.

## Level 4: The client side: ClojureScript
Now for the ClojureScript. Note that ClojureScript is still in Alpha and it is bound to change.
At least this code is working at the time of writing.

Create a file `src/html/janusclojure.cljs`. Yes, that exposes the source to the outside world.
This might not be what you want, after all people might read your code and learn something (or
even worse: find bugs for you). Now, after all the compiled JavaScript that is produced by ClojureScript
may be obfuscated, but it is not encryptet. No real secrets there... Anyway, here is the (first) contents of
the cljs-file:

    (ns janusclojure
      (:require
        [cljs.reader :as reader]
        [goog.dom :as dom]
        [goog.net.XhrIo :as ajax2]
        [goog.events :as events]
        [goog.events.EventType :as event-type]
      ))

    (defn log [str]
        (js* "console.log(~{str})"))

    (def random-text-input
        (dom/getElement "random-text"))

    (defn fetch-clicked [e]
       (goog.net.XhrIo/send
           "/uuid"
           (fn [e]
             (let [xhr (.target e)
                   respTxt (. xhr (getResponseText))
                   data (reader/read-string respTxt)
                   ]
               (log respTxt)
               (set! (.value random-text-input) (:uuid data))))))

    (events/listen
        (dom/getElement "getUUID")
        event-type/CLICK
        fetch-clicked)

There is a hole lot of stuff (but still quite little code). Some notes about this:

*. The log function is just a litte debug helper. Unfortunately JavaScript has the tendency to silently swallow
errors. Even the ClojureScript compiler is most of the time very silent. Some (log) commands are quite usefull.
Also note that Google Closure has much more sofisticated logging possibilities, here I just generate a console.log
command. This also shows (off) the `js*` command, which produces raw JavaScript.
*. `random-text-input` is the HTML element with the id `random-text`. I could write that dom/getElement inline
as well, but
    1. The thing is inlined by the Google Closure compiler (if that would give a shorter code)
    1. Later we will reuse that element anyway.
*. `events/listen` binds the "onClick" event to the button (with the id `getUUID`) to the function `fetch-clicked`.
*. `fetch-clicked` is executed when the user clicks the "getUUID" button.

The function creates aXhrIo/send object, more specific a "GET" call to the given URL `/uuid`.
The following function (nameless) is executed later when the result arrives.

AJAX at its best, though in our case we are not using XML, not even the normal JSON as a payload, but real good home-made
serialized Clojure data.

To read that into a form that suits ClojureScript (that is: JavaScript), the result
is parsed using `reader/read-string`. From then on it is easy to work with, `(:uuid data)` extracts the UUID.

Last not least we set the value property of the random-text-input (which is the HTML input field), `(.value random-text-input)`,
to the UUID-string we got back.

Next we have to compile the cljs-file. Compiling is slow, if you fire up the JVM every time. But within a running JVM
instance it is relatively quick. I myself added the following to my `.bashrc`:

    CLOJURESCRIPT_HOME=~/clojure/clojurescript
    PATH=$PATH:$CLOJURESCRIPT_HOME/bin:$CLOJURESCRIPT_HOME/script

Also I recommend that you install `rlwrap` to quickly repeat earlier commands in REPLs (and other text programs)
using the cursor keys. Now `cd src/html` and run:

    rlwrap repl

This starts the ClojureScript REPL. Now initialize the compiler with

    (require '[cljs.closure :as cljsc])

and then you can compile the cljs file with

    (cljsc/build "janusclojure.cljs" {:optimizations :advanced :pretty-print false :output-to "janusclojure.js"})

This makes a fully optimized compilation, if you would like to be able to recognize your code, change to

    (cljsc/build "janusclojure.cljs" {:optimizations :simple :pretty-print true :output-to "janusclojure.js"})

You can also compile without any optimizations, but I will not cover that here.

Last not least, add the generated `janusclojure.js` to the HTML page by adding this line just before the closing `</body>'
almost at the end of the `index.html` file:

    <script type="text/javascript" src="janusclojure.js"></script>

Now to test THAT you do not have to restart the Jetty instance. Just reload the index.html (you may need to force reload
by pressing *Ctrl-R*). Click on the "Fetch" button and you should see some random text.

Now that was probably the hardest part. An active client HTML page that connects to the server. Next comes...

## Level 5: sending data to the server

TBD


## Putting it on the server

In the `janusclojure/` diretory:

    $ git init
    $ echo src/html/out >>.gitignore
    $ git add .
    $ git commit -m "Initial check in"

For Heroku:

    $ heroku create --stack cedar
    $ git push heroku master


## Usage

Besides this *README* there are all the files in the repository. You might want to check out the project which contains all the files.
They should be usable right away (i.e. you can `lein run` them or push them to Heroku).


## Feedback
Feedback is very welcome. If you find stuff that could be done better (shorter or clearer, preferably), please
let me know: megapatch@gmail.com

## License

The JanusClojure example is stands under the [Creative Commons License](http://creativecommons.org/licenses/by/3.0/).

## Resources and links
* Clojure on Heroku: https://gist.github.com/1001206 
