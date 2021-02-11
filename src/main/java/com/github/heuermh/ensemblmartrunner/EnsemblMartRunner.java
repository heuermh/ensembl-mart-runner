/*

    ensembl-mart-runner  Ensembl Mart runner.
    Copyright (c) 2021 held jointly by the individual authors.

    This library is free software; you can redistribute it and/or modify it
    under the terms of the GNU Lesser General Public License as published
    by the Free Software Foundation; either version 3 of the License, or (at
    your option) any later version.

    This library is distributed in the hope that it will be useful, but WITHOUT
    ANY WARRANTY; with out even the implied warranty of MERCHANTABILITY or
    FITNESS FOR A PARTICULAR PURPOSE.  See the GNU Lesser General Public
    License for more details.

    You should have received a copy of the GNU Lesser General Public License
    along with this library;  if not, write to the Free Software Foundation,
    Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307  USA.

    > http://www.fsf.org/licensing/licenses/lgpl.html
    > http://www.opensource.org/licenses/lgpl-license.php

*/
package com.github.heuermh.ensemblmartrunner;

import static org.dishevelled.compress.Readers.reader;
import static org.dishevelled.compress.Writers.writer;

import java.io.BufferedReader;
import java.io.File;
import java.io.PrintWriter;

import java.net.URLEncoder;

import java.util.concurrent.Callable;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

import org.dishevelled.commandline.ArgumentList;
import org.dishevelled.commandline.CommandLine;
import org.dishevelled.commandline.CommandLineParseException;
import org.dishevelled.commandline.CommandLineParser;
import org.dishevelled.commandline.Switch;
import org.dishevelled.commandline.Usage;

import org.dishevelled.commandline.argument.FileArgument;

//import org.slf4j.Logger;
//import org.slf4j.LoggerFactory;

/**
 * Ensembl Mart runner.
 *
 * @author  Michael Heuer
 */
public final class EnsemblMartRunner implements Callable<Integer> {
    private final File inputMartXmlFile;
    private final File outputTextFile;
    private final OkHttpClient client = new OkHttpClient();
    //private final Logger logger = LoggerFactory.getLogger(EnsemblMartRunner.class);
    private static final String BASE_URL = "http://www.ensembl.org/biomart/martservice?query=";
    private static final String USAGE = "ensembl-mart-runner -i input.mart.xml.gz -o output.txt.gz";


    /**
     * Create a new Ensembl Mart runner.
     *
     * @param inputMartXmlFile input Ensembl Mart XML file, if any
     * @param outputTextFile output text file, if any
     */
    public EnsemblMartRunner(final File inputMartXmlFile, final File outputTextFile) {
        this.inputMartXmlFile = inputMartXmlFile;
        this.outputTextFile = outputTextFile;
    }


    @Override
    public Integer call() throws Exception {
        try (BufferedReader reader = reader(inputMartXmlFile); PrintWriter writer = writer(outputTextFile)) {

            // build Mart request URL
            StringBuilder sb = new StringBuilder();
            while (reader.ready()) {
                sb.append(reader.readLine());
            }
            String xml = sb.toString();
            String url = BASE_URL + URLEncoder.encode(xml, "UTF-8");

            //logger.info("url=" + url);

            // build request
            Request request = new Request.Builder()
                .url(url)
                .build();

            //logger.info("request=" + request);

            // stream response to writer
            try (Response response = client.newCall(request).execute()) {

                //logger.info("response=" + response);

                try (BufferedReader streamReader = new BufferedReader(response.body().charStream())) {
                    // block until at least one line is seen
                    String line = streamReader.readLine();

                    //int count = 1;
                    while (line != null) {
                        writer.println(line);
                        line = streamReader.readLine();
                        //count++;
                    }
                    //logger.info("read " + count + " lines");
                }
                /*
                  streamReader is not ready even though response is 200 OK

                try (BufferedReader streamReader = new BufferedReader(response.body().charStream())) {
                    while (streamReader.ready()) {
                        writer.println(streamReader.readLine());
                    }
                }
                */
            }
            return 0;
        }
    }

    /**
     * Main.
     *
     * @param args command line args
     */
    public static void main(final String[] args) {
        Switch about = new Switch("a", "about", "display about message");
        Switch help = new Switch("h", "help", "display help message");
        FileArgument inputMartXmlFile = new FileArgument("i", "input-mart-xml-file", "input Ensembl Mart XML file, default stdin", false);
        FileArgument outputTextFile = new FileArgument("o", "output-text-file", "output text file, default stdout", false);

        ArgumentList arguments = new ArgumentList(about, help, inputMartXmlFile, outputTextFile);
        CommandLine commandLine = new CommandLine(args);

        EnsemblMartRunner ensemblMartRunner = null;
        try {
            CommandLineParser.parse(commandLine, arguments);
            if (about.wasFound()) {
                About.about(System.out);
                System.exit(0);
            }
            if (help.wasFound()) {
                Usage.usage(USAGE, null, commandLine, arguments, System.out);
                System.exit(0);
            }
            ensemblMartRunner = new EnsemblMartRunner(inputMartXmlFile.getValue(), outputTextFile.getValue());
        }
        catch (CommandLineParseException e) {
            if (about.wasFound()) {
                About.about(System.out);
                System.exit(0);
            }
            if (help.wasFound()) {
                Usage.usage(USAGE, null, commandLine, arguments, System.out);
                System.exit(0);
            }
            Usage.usage(USAGE, e, commandLine, arguments, System.err);
            System.exit(-1);
        }
        catch (NullPointerException | IllegalArgumentException e) {
            Usage.usage(USAGE, e, commandLine, arguments, System.err);
            System.exit(-1);
        }
        try {
            System.exit(ensemblMartRunner.call());
        }
        catch (Exception e) {
            e.printStackTrace();
            System.exit(1);
        }
    }
}
