Storage Exchange
=====================================

## Get running

To start the local web server, simply type `play run`.

## Committing new code

Do not commit directly to master, as that code will be put 
right away in production. Whenever you develop, make a branch
with your name, and what you are working on, and then send Michele a PR.
For example, `michele-redesignHomepage` would be an appropriate name.
Try to be as specific as possible

## Write Unit tests

Code that is not tested will not be merged in. Be sure to write
plenty of tests. There are three ways that you can write tests 
is play:
  1) Unit test,
  2) Integration test,
  3) Property-based tests, with [http://www.scalacheck.org/].

Play has a way to test just about anything you want. It integrates with Mockito and a lot of other stuff,
to make your testing experience just great. There are ideomatic ways to test controllers and models, just 
ask me how to do, and I will help you out.

## Deploying

We will be deploying using Heroku. 
You will be able to access the application at [http://storagexchange.herokuapp.com].
If you feel like you want direct access to Heroku, I can put up a staging server 
so that you can better test your commits. Just let me know and I'll set it up.
This file will be packaged with your application, when using `play dist`.

## Style

Before making a commit, always check that you comply with Scala coding styles.
You can run `play scalastyle` to have an automated checker running through your code.

## Set up IDE

Its very simple to start using Eclipse, and I highly recommend doing so. Download the scala IDE from here: [http://scala-ide.org/]. Then, create Eclipse files by typing `play eclipse with-source=true`.
