# mdchat

A simple, IRC-like chat app for iPhone, as an exercise in writing an iOS app and 
a server for it to talk to, mostly from scratch.


## mdchatserver

Simple webservice for chat, providing users, topics, and messages. More or less 
RESTful, with just a handful requests, all returning JSON. No authentication is 
done; the server simply tracks the user it is given for each message. The API 
only collects new data; once added, nothing can be edited or deleted.

Implemented with Jersey, using [neo4j](http://neo4j.org) for persistence. In fact, 
this project could be seen as a simple demo of how to implement a RESTful 
web service using neo4j.

There are a handful of integration tests that exercise and demo the API.

### Running the server

Only JDK 1.7 is required. A self-bootstrapping Gradle build script is included; 
this single command builds and runs the server:
`./gradlew run`

On Mac OS X, Gradle sometimes doesn't get along well with Oracle's strange way 
of deploying the JDK, and this alternative wrapper might help:
`./gw7 run`

### Exploring the API

All the data can be explored just by starting from the server root: 

`http://localhost:9998/`

This is much more pleasant experience if you have something like the 
[JSONView](http://jsonview.com/) browser extension, which makes the links clickable 
and also formats the JSON nicely for reading.

New topics and users can be added by simply requesting them:

`http://localhost:9998/users/steve` -- creates new user `steve` if necessary

`http://localhost:9998/topics/underwaterBasketWeaving` -- similar

To post, you can make a POST request to the topic:

`http://localhost:9998/topics/pets?user=steve&text=I+like+cats`

or, equivalently, GET to this special URL (just to make in-browser testing easier):

`http://localhost:9998/topics/post?topic=pets&user=steve&text=I+like+cats`


The server doesn't do much error handling, and is probably easily confused by 
things like user/topic names with spaces.


## mdchatphone

_Very_ simple iPhone chat app. Basically functional, but it's not pretty and 
lacks many basic features.

You'll need Xcode (ca. iOS 7) to build and do anything with it. I have run it only 
in the simulator. May cause your phone to collapse in fits of laughter if you deploy 
it to an actual device.

This is the largest and most sophisticated iOS program I have ever written, by a 
large margin. No one should treat this code as an example of how to do anything.

Most of the intelligent ideas were taken from the Stanford iOS 7 class (available
[here](https://itunes.apple.com/us/course/developing-ios-7-apps-for/id733644550)).

### Usage

So, how does one test or demo a chat app using a single (simulated) device? One 
answer is, you post a message or two, then go to the Settings screen, change your user name, and then post some more. No, it's not particularly slick.

Alternatively, use a browser to post some messages under different user names and then 
exit and re-enter the topic to see them appear.

Note: the Topics list can be reloaded using the "pull to refresh" gesture, but that 
doesn't work on the messages screen. Trust me, it's hard--it's got to do with the 
ViewController not being a UITableViewController because there's a parent view... 
You probably don't want to know the details.


## License

Call it public domain. Use at your own risk. Seriously.
