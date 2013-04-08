package com.metadave.contactweb;

import java.io.File;
import java.io.IOException;

import static spark.Spark.get;
import static spark.Spark.post;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.net.URL;
import java.util.List;
import java.util.Map;

import com.basho.contact.*;
import com.basho.contact.parser.ContactLexer;
import com.basho.contact.parser.ContactParser;
import org.antlr.v4.runtime.ANTLRInputStream;
import org.antlr.v4.runtime.CommonTokenStream;
import org.antlr.v4.runtime.tree.ParseTreeWalker;

import org.apache.commons.cli.*;
import org.apache.commons.lang.StringEscapeUtils;
import spark.Request;
import spark.Response;
import spark.Route;



public class App {

    static WebSecurityPolicy policy = new WebSecurityPolicy();
    static ContactConnectionProvider connections;
    // slow for now
//    public String readFileAsString(String path) {
//        String page = "";
//        try {
//            File f = new File(path);
//            page = org.apache.commons.io.FileUtils.readFileToString(f);
//        } catch (IOException e) {
//            e.printStackTrace();
//        }
//        return page;
//    }

    public static String loadResource(String name) {
        File f = new File(name);
        String result = "";
        try {
            result = org.apache.commons.io.FileUtils.readFileToString(f);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return result;
    }

    public App(String dr) {
        final String docroot = dr;
        get(new Route("/") {
            @Override
            public Object handle(Request request, Response response) {
                String path = docroot + "/index.html";
                return loadResource(path);
            }
        });

        post(new Route("/process") {
            @Override
            public Object handle(Request request, Response response) {

                if(request.session().isNew()) {
                    System.out.println("Creating session info");
                    RuntimeContext ctx = new RuntimeContext(connections, null, null);
                    ctx.setAccessPolicy(policy);
                    ContactWalker walker = new ContactWalker(ctx);
                    request.session().attribute("ctx",ctx);
                    request.session().attribute("walker", walker);
                }

                ByteArrayOutputStream bout = new ByteArrayOutputStream();
                ByteArrayOutputStream berr = new ByteArrayOutputStream();
                PrintStream pout = new PrintStream(bout);
                PrintStream perr = new PrintStream(berr);

                RuntimeContext ctx = (RuntimeContext)request.session().attribute("ctx");

                ctx.resetIO(pout, perr);
                ContactWalker walker = (ContactWalker)request.session().attribute("walker");

                String command = request.queryParams("command");
                System.out.println(command);
                processInput(command, walker, ctx);
                System.out.println("Processed input " + command);

                System.out.println("Processed output " + command);

                String outstr = StringEscapeUtils.escapeHtml(bout.toString());
                String errstr = StringEscapeUtils.escapeHtml(processErrors(ctx));
                String strresp = buildResponse(outstr, errstr);
                System.out.println("Response = " + strresp);
                bout.reset();
                berr.reset();
                return strresp;
            }
        });
    }

    private String buildResponse(String output, String error) {
        // eh, I lied
        if(error != null && !error.equals("")) {
            return "Error:" + error;
        } else {
            return output;
        }
    }


    private static void processInput(String line, ContactWalker cw, RuntimeContext runtimeCtx) {
        ANTLRInputStream input = new ANTLRInputStream(line);
        ContactLexer lexer = new ContactLexer(input);
        CommonTokenStream tokens = new CommonTokenStream(lexer);
        ContactParser parser = new ContactParser(tokens);

        // combine these two into one
        parser.removeErrorListeners();
        parser.addErrorListener(new WebErrorListener(runtimeCtx));

        ParseTreeWalker walker = new ParseTreeWalker();
        try {
            walker.walk(cw, parser.prog());
        } catch (Throwable t) {
            // catch parse errors. ANTLR will display a message for me.
        }

    }

    private static String processErrors(RuntimeContext runtimeCtx) {
        StringBuilder errorString = new StringBuilder();

        List<Throwable> errors = runtimeCtx.getErrors();
        for (Throwable t : errors) {
            StringBuilder buf = new StringBuilder();
            buf.append(t.getMessage());
            if (t.getCause() != null) {
                buf.append(":" + t.getCause().getMessage());
            }
            buf.append("\n");
            errorString.append(buf.toString());
        }
        runtimeCtx.reset();
        return errorString.toString();
    }


    @SuppressWarnings("static-access")
    public static CommandLine processArgs(String[] args) {
        Options options = new Options();

        Option hosts = OptionBuilder
                .withLongOpt("hosts")
                .withDescription("Comma separated list of Riak riak_host:riak_pb_port")
                .hasArg()
                .withArgName("line")
                .create();

        Option docroot = OptionBuilder
                .withLongOpt("docroot")
                .withDescription("Full path to web directory")
                .hasArg()
                .withArgName("line")
                .create();

        options.addOption(hosts);
        options.addOption(docroot);

        CommandLineParser parser = new org.apache.commons.cli.GnuParser();
        try {
            CommandLine line = parser.parse(options, args);
            if (line.hasOption("help") || !line.hasOption("hosts")) {
                HelpFormatter formatter = new HelpFormatter();
                formatter.printHelp("contact_web", options);
                System.exit(0);
            }
            return line;
        } catch (ParseException exp) {
            System.err.println("Error processing command line args: " + exp.getMessage());
            System.exit(-1);
        }
        return null;
    }

    public static void main(String[] args) {
        CommandLine cl = processArgs(args);
        connections = new WebConnectionProvider(cl.getOptionValue("hosts"));
        String docroot = cl.getOptionValue("docroot");
        System.out.println("Docroot = " + docroot);
        new App(docroot);
    }


}
