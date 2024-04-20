# Alexa Skill - ExtraAbilities

This project is a complete Alexa skill in production use.  It basically allows users to create any number of public 
(within the family) or private lists.

It was developed as a learning and testing experience.  You
can use it by saying "Alexa Enable Skill Extra Abilities".  You can also go to the site 
[https://extraabilities.biz](https://extraabilities.biz) for more information about the skill.

The way this project was built has some interesting history.  At first, I looked at the Alexa Developer Library.
I thought it was unnecessarily cumbersome and complex to use.  I, therefore, decided not to make any use of their developer 
API and just write my own.  Since they were just using JSON REST web services, there wasn't much to it.  That
proved successful.

After the first success, I found myself frustrated with two facts:

1. A significant part of my code resided on Amazon, making my application significantly incomplete (since all the 
parsing code was on Amazon's site).

2. I had to go through Amazon's approval process for each little change.  This meant a lot of development delays.

I found a way to do all the parsing on my end.  This alleviated both of those problems.  However, since Amazon has
less information about what it should expect people to say, Alexa's ability to interpret is diminished.  This also
complicated the code on my end since I had to do all the parsing.

The version I am uploading is the final version in which I do all the parsing myself.

This system was built on an old version of my Java-based KISS, open-source, full-stack web development framework
(included herein).  The latest version (and in production) of KISS is available at
[https://kissweb.org](https://kissweb.org) or [https://github.com/blakemcbride/Kiss](https://github.com/blakemcbride/Kiss)

