# ensembl-mart-runner
Ensembl Mart runner.

[![Maven Central](https://img.shields.io/maven-central/v/com.github.heuermh.ensemblmartrunner/ensembl-mart-runner.svg?maxAge=600)](http://search.maven.org/#search%7Cga%7C1%7Ccom.github.heuermh.ensemblmartrunner)

### Hacking ensembl-mart-runner

Install

 * JDK 1.8 or later, https://openjdk.java.net
 * Apache Maven 3.6.3 or later, https://maven.apache.org

To build

    $ mvn install


### Running ensembl-mart-runner

```
$ ensembl-mart-runner  --help
usage:
ensembl-mart-runner -i input.mart.xml.gz -o output.txt.gz

arguments:
   -a, --about  display about message [optional]
   -h, --help  display help message [optional]
   -i, --input-mart-xml-file [class java.io.File]  input Ensembl Mart XML file, default stdin [optional]
   -o, --output-text-file [class java.io.File]  output text file, default stdout [optional]
```

#### Compression

With `ensembl-mart-runner`, stdin and stdout should behave as expected, and files and streams
compressed with Zstandard (zstd), GZIP, BZip2, and block-compressed GZIP (BGZF) are handled
transparently. Use file extensions `.zst`, `.gz`, `.bz2`, and `.bgz` respectively to force the
issue, if necessary.
