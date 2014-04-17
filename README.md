Classic problem (often used in interview coding assignments):

  * We a have large dataset represented as a set of lines in the text file.
    The file is too large to fit in a single machine memory.
  * We need to remove all duplicate lines in it.
  * Order of the lines in the final result is not important.

How would you approach it using Akka?
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
processing the line, passing back the line itself if not a duplicate, otherwise passes "None".
The FileProcessor writes the lines it gets back immediately to the output file.
When all lines have been processed we're done (close the files).

With this solution, the original order of the lines is not preserved.

Fault tolerance: Since each actor holds the state of the work in progress, if an
actor goes down then that work is lost. Recovery would be possible with the use
of a database, see below.

###Alternative Implementations###
1. **Use a database**. Use of a distributed database such as Cassandra or Riak, et al.,
would make the use of multiple actors less significant for solving this problem since
the database already has consistent hashing across multiple machines. Futhermore,
the persistence provided by the database could make the work restartable in case of
failure. The downside, of course, is that the program would not perform as well since
the data would need to be written to the commit logs before the write operations are
able to complete.
2. **Preserve order**. To preserve the original order of the lines, one way would be to
attach a line number to each line. The line processors to keep an index of the line numbers
for the lines. Instead of using consistent hashing for routing, use
partitioning based on ranges of line numbers. The line processors must wait until the end of
file processing before output is sent. When the line processors send their
output back to the file processor they do so in an "orderly" fashion, coordinated by the
file processor, going from one partition to another in order. Each output line is acknowledged
by the file processor before the next line is sent. This could also be done in batches of
lines in contiguous order. 

With thanks to Lucian Cancescu for his idea for performance improvement.