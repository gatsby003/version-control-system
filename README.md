# version-control-system

Project done as part of a Data Structures course Hug61B taught by UC Berkely's <a href="https://joshhug.gitbooks.io/hug61b/content/">Josh Hug</a>.

A java based implementation of the git version control system. Implements all the basic version control functionalities along with checkout and merging.


* Classes 
  * Main : the main class
  * Repository : the repository class handles implementing all the commands
  * Commit : models the commit objects, provides helper methods for dealing with commits
  * Stage : models the staging area, provides methods for staging and un-staging files.
  

# Commands

* init : initialises the repository
* add [filename] : stages file for commit
* commit -m [message] : creates a commit object
* log , global-log : displays the information about commits
* find [commit message] : searches for the commit with the given message
* status : prints status of the staging area
* checkout : checks out a branch, file or a particular commit
* branch [name] : creates a new branch 
* rm-branch : deletes branch
* reset : checks out files tracked by a given commit
* merge [branch_name] : merges two branches , also detects merge conflicts

