So here is a classical problem:

  * We a have large dataset represented as a set of lines in the text file. And large means that it cannot fit in a single machine memory for sure.
  * We need to remove all duplicate lines in it.
  * Order of the lines in the final result is not important.

How would you approach it having Akka/Scala in your toolbelt?
You are free to use akka-cluster or/and remoting if necessary.
You are free to use more than one machine for processing.

[Optional]: Order of lines in the result is important.


Solution
--------

To run the solution, cd to the project directory and run:
> sbt  
> run src/test/resources/test.txt out.txt

The test.txt file is a randomly generated input file and out.txt is
of course the output file with duplicates removed.

This solution uses the consistent-hashing router provided by akka.
The router can be configured in the application.conf file with the number
of instances, the virtual node factor, and also a RemoteRouterConfig can be
used to divide the instances amongst several machines in a cluster.

The router is used to divide the work between some number of actors that each
process lines (LineProcessor). Each LineProcessor keeps a mutable Set of lines (Strings).
By using a Set the duplicates are automatically removed.

The object structure of the application is as follows:

* App (RemoveDuplicates)
    * FileProcessor (Actor)
        * ConsistentHashingRouter
            * N instances of LineProcessor


The lines in the file are sent in messages from the FileProcessor to the LineProcessors
via the router. The LineProcessor notifies the FileProcessor when it is has finished
processing the line. When all lines have been processed, the FileProcessor sends a
broadcast message to all the LineProcessors to send it all the non-duplicate lines,
which it then outputs to the output file.

With this solution, the original order of the lines is not preserved.

Fault tolerance: Since each actor holds the state of the work in progress, if an
actor goes down then that work is lost. Recovery would be possible with the use
of a database, see below.

###Alternative Implementations###
1. Use a database. Use of a distributed database such as Cassandra or Riak, et al.,
would make the use of multiple actors less significant for solving this problem since
the database already has consistent hashing across multiple machines. Futhermore,
the persistence provided by the database could make the work restartable in case of
failure. The downside, of course, is that the program would not perform as well since
the data would need to be written to the commit logs before the write operations are
able to complete.
2. Preserve order. To preserve the original order of the lines, a line number could be attached
to each line. Because the input file is read in order by the FileProcessor, it should
be deterministic that the first copy of duplicate lines would be kept and subsequent
copies removed. This comes from the guarantee of the ordering of messages from one
actor to another. With the consistent hashing algorithm, the same actor will always
process all the duplicates of a line. To put the file back together again in the original
order would be difficult, and the use of a database or filesystem would be a big help
here to provide the sorting.

###Still work to do###
I didn't have the time yet to properly unit test the code. There is a scala
workspace, however, that can generate large files with random content
including duplicates.
