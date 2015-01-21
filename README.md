# Zeromeaner

Zeromeaner is a falling-blocks game forked from the immensely popular (and and immensely abandoned years ago) project [NullpoMino](https://code.google.com/p/nullpomino/) .  I recommend downloading the [latest snapshot build](http://www.zeromeaner.org/unstable/zeromeaner.jar)  for now, since I haven’t yet made an official release of version 1.2.0.

## Project Website

Zeromeaner can be found on the web at [www.zeromeaner.org](http://www.zeromeaner.org/).

## TL;DR for contributors

Zeromeaner is hosted on [stash.robindps.com](http://stash.robindps.com/projects/ZRM/repos/zeromeaner/browse).  The github repo, [found here](https://github.com/zeromeaner/zeromeaner), is just a mirror of the definitive source hosted on [stash.robindps.com](http://stash.robindps.com/projects/ZRM/repos/zeromeaner/browse).

Zeromeaner's issue tracker can be found at [jira.robindps.com](http://jira.robindps.com/browse/ZRM/?selectedTab=com.atlassian.jira.jira-projects-plugin:summary-panel).  Please use this JIRA rather than github's built-in issue tracker.  If I notice any issues on the github tracker I will try to bring them over to JIRA.

Zeromeaner's continuous integration can be found at [bamboo.robindps.com](http://bamboo.robindps.com/browse/ZRM).

## Blurb

Zeromeaner was forked over two years ago, and a significant amount of the original source code has been rewritten.  (On a side note, it seems that NullpoMino was written by expert tetris players, rather than expert programmers, and it shows in the code.  Conversely, I’m an expert programmer and a terrible tetris player.  Oh well.)  There have been lots of improvements since NullpoMino’s last commit over two years ago.

Zeromeaner is also fun just because.

Feel free to extend or embed Zeromeaner to suit your own purposes.  For purposes of easter eggs, however, you may wish to consider using [eviline](http://www.eviline.org/) , a sister project to develop a powerful tetris AI that can be used to populate your easter egg with the worst possible sequence of shapes.

## (Lack of) Applet Support Rant

In its previous incarnation, zeromeaner was provided (in addition to downloads) as a Java applet playable in a web browser.  This capability was discontinued when Oracle introduced the requirement that all applets be digitially signed by a trusted (and expensive) thid party identification verification service.  Oracle proceeded to further screw over the possibility of Java applet games by breaking the KeyListener API, and they have shown no signs of an inclination to fix it.  For now please just use the downloads menu.  If Oracle ever gets around to fixing the KeyListener API, I’ll fork over the cash to buy a certificate so you can play Zeromeaner in your browser again.

## License

Zeromeaner is released under a BSD license.  The components of Zeromeaner inherited from NullpoMino are also released under a BSD license.

