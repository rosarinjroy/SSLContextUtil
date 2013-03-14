package com.rosarinjroy;

/**
 * Copyright (c) 2013, Rosarin Roy (roarinjroy at hotmail dot com)
 * All rights reserved.
 *
 * Redistribution and use in source and binary forms, with or without
 * modification, are permitted provided that the following conditions are met:
 *
 * 1. Redistributions of source code must retain the above copyright notice, this
 *    list of conditions and the following disclaimer.
 * 2. Redistributions in binary form must reproduce the above copyright notice,
 *    this list of conditions and the following disclaimer in the documentation
 *    and/or other materials provided with the distribution.
 *
 * THIS SOFTWARE IS PROVIDED BY THE COPYRIGHT HOLDERS AND CONTRIBUTORS "AS IS" AND
 * ANY EXPRESS OR IMPLIED WARRANTIES, INCLUDING, BUT NOT LIMITED TO, THE IMPLIED
 * WARRANTIES OF MERCHANTABILITY AND FITNESS FOR A PARTICULAR PURPOSE ARE
 * DISCLAIMED. IN NO EVENT SHALL THE COPYRIGHT OWNER OR CONTRIBUTORS BE LIABLE FOR
 * ANY DIRECT, INDIRECT, INCIDENTAL, SPECIAL, EXEMPLARY, OR CONSEQUENTIAL DAMAGES
 * (INCLUDING, BUT NOT LIMITED TO, PROCUREMENT OF SUBSTITUTE GOODS OR SERVICES;
 * LOSS OF USE, DATA, OR PROFITS; OR BUSINESS INTERRUPTION) HOWEVER CAUSED AND
 * ON ANY THEORY OF LIABILITY, WHETHER IN CONTRACT, STRICT LIABILITY, OR TORT
 * (INCLUDING NEGLIGENCE OR OTHERWISE) ARISING IN ANY WAY OUT OF THE USE OF THIS
 * SOFTWARE, EVEN IF ADVISED OF THE POSSIBILITY OF SUCH DAMAGE.
 */

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;

import javax.net.ssl.SSLContext;

import com.rosarinjroy.ssl.SSLContextFactory;

public class Main {
    private static String keyStorePath = null;
    private static String keyStorePassword = null;
    private static String keyStoreType = null;
    private static String securityProvider = null;
    private static String url = null;
    private static String protocol = null;

    public static void main(String[] args) throws IOException {
        parseArgs(args);
        if(keyStorePath == null || keyStorePassword == null) {
            printHelpAndExit("You must specify key store path and password as arguments.");
        }

        SSLContextFactory factory = new SSLContextFactory();
        factory.setKeyStorePath(keyStorePath);
        factory.setKeyStorePassword(keyStorePassword);
        factory.setKeyStoreType(keyStoreType);
        factory.setProtocol(protocol);
        factory.setSecurityProvider(securityProvider);
        factory.setPerformStrictNameMatching(false);
        factory.init();
        System.out.println("SSLContextFactory initialized successfully: " + factory);
        SSLContext sslContext = factory.getSSLContext();
        System.out.println("Successfully created SSLContext");

        if(url != null) {
            System.out.println("Connecting to: [" + url + "]");
            SSLContext.setDefault(sslContext);
            URL urlObj = new URL(url);
            URLConnection urlConn = urlObj.openConnection();
            BufferedReader bis = new BufferedReader(new InputStreamReader(urlConn.getInputStream()));
            String line;
            while((line = bis.readLine()) != null) {
                System.out.println("< " + line);
            }
        }
    }

    private static void parseArgs(String[] args) {
        if(args.length == 0) {
            printHelpAndExit("Expected at least the key store path and password as arguments.");
        }

        int i = 0;
        while(i < args.length) {
            if(args[i].equals("-h")) {
                printHelpAndExit(null);
            } else if (args[i].equals("-k")) {
                i++;
                if(i == args.length) {
                    printAndExit("-k requires an argument.");
                }
                keyStorePath = args[i];
            } else if (args[i].equals("-p")) {
                i++;
                if(i == args.length) {
                    printAndExit("-p requires an argument.");
                }
                keyStorePassword = args[i];
            } else if (args[i].equals("-t")) {
                i++;
                if(i == args.length) {
                    printAndExit("-t requires an argument.");
                }
                keyStoreType = args[i];
            } else if (args[i].equals("-v")) {
                i++;
                if(i == args.length) {
                    printAndExit("-v requires an argument.");
                }
                protocol  = args[i];
            } else if (args[i].equals("-u")) {
                i++;
                if(i == args.length) {
                    printAndExit("-u requires an argument.");
                }
                url = args[i];
            } else if (args[i].equals("-s")) {
                i++;
                if(i == args.length) {
                    printAndExit("-u requires an argument.");
                }
                securityProvider = args[i];
            }

            i++;
        }
    }

    private static void printAndExit(String message) {
        System.err.println(message);
        System.err.println("Try -h option to get help.");
        System.exit(1);
    }

    private static void printHelpAndExit(String message) {
        if(message != null) {
            System.err.println(message);
        }
        System.err.println();
        System.err.println("SSL Context Utils - Written by Roy (rosarinjroy at hotmail dot com)");
        System.err.println("Accepted options:");
        System.err.println(" -k - Key store path");
        System.err.println(" -p - Password for the key store");
        System.err.println(" -t - Key store type (default will be guessed from the file extension)");
        System.err.println(" -v - Protocol version (default is TLS)");
        System.err.println(" -s - Security provider (default SunJSSE)");
        System.err.println(" -u - URL to connect to (for e.g. https://secureserver.com/index.html)");
        System.err.println(" -h - To print this message and exit");
        System.exit(1);
    }
}
