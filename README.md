# Fragments
What happens when you mix Dark Souls style messages with Pokemon Go and YikYak? You get Fragments.

## Inspiration
![Dark Souls message](https://i.imgur.com/m4nXHfG.jpg)

Dark Souls only lets player talk by dropping messages in the game world. Other players can read dropped messages, so players share secrets, fun strategies, and other silly things. What would it be like if there was an entire social media platform based on this idea? Thus Fragments was born.

Fragments is unique because it requires all its users to be in a specific area in order to view messages. This unique dynamic will lead to messages that rely on the location's context. For example, messages at a bus stop can be about which lines are usually late.

## What it does
![bark bark bark bark bark bark bark bark bark bark bark bark](https://challengepost-s3-challengepost.netdna-ssl.com/photos/production/software_photos/000/601/622/datas/gallery.jpg)

Fragments is an Android App that lets users drop 'fragments', short messages, on the map. Other users can see dropped fragments, but they have to be close enough to read the message. 

## How I built it
With lots and lots of [eurobeat](https://www.youtube.com/watch?v=ItjjWECjD_M)

![The back end, OwO](http://i.imgur.com/E3OcLHz.png)

I used Android Studio with Java to make the Android App. For the data server, I used python and flask to create a REST API just for Fragments. The data server also has a SQL database for quick querying.

## Challenges I ran into
This is the first Android App I made. It was difficult getting used to programming on Android. There are certain restrictions and events that an Android App must accommodate that a regular Java application does not.
Another challenge I ran to was GPS and location data. Phones can use both GPS and network location and I had to fuse the two location providers in order to get the most accurate reading. 
Lastly, this is the first time using a SQL database to hold all the data. It was challenging learning the SQL syntax, but it worked out in the end.

## Accomplishments that I'm proud of
I didn't die.

I'm proud of how well the app turned out, I worked very hard over the weekend. I was worried that the app will never work out to the scope I wanted but it turned out great in the end.

## What I learned
I learned that App development is quite fun! Creating a UI was rather quick and easy compared to using something like JavaFX.

## What's next for Fragments
I would love to actually publish this app on the Play Store. I want to see what other people will use the location based messages. I hope people use it to share tips to other people, thus helping each other out.
