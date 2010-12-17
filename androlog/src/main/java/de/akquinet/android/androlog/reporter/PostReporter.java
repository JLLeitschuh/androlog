package de.akquinet.android.androlog.reporter;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.charset.Charset;
import java.util.Properties;

import android.content.Context;
import de.akquinet.android.androlog.Log;

/**
 * Reporter posting the report to a given URL.
 * It uses a plain HTTP POST.
 */
public class PostReporter implements Reporter {

    /**
     * Mandatory Property to set the URL.
     */

    public static final String ANDROLOG_REPORTER_POST_URL = "androlog.reporter.post.url";

    /**
     * The URL object.
     */
    private URL url;

    /**
     * Configures the POST Reporter.
     * The given configuration <b>must</b> contain the {@link PostReporter#ANDROLOG_REPORTER_POST_URL}
     * property and it must be a valid {@link URL}.
     * @see de.akquinet.android.androlog.reporter.Reporter#configure(java.util.Properties)
     */
    @Override
    public void configure(Properties configuration) {
        String u = configuration.getProperty(ANDROLOG_REPORTER_POST_URL);
        if (u == null) {
            Log.e(this,
                    "The Property " + ANDROLOG_REPORTER_POST_URL + " is mandatory");
            return;
        }
        try {
            url = new URL(u);
        } catch (MalformedURLException e) {
            Log.e(this,
                    "The Property " + ANDROLOG_REPORTER_POST_URL + " is not a valid url", e);
        }
    }

    /**
     * If the reporter was configured correctly, post the report to the set URL.
     * @see de.akquinet.android.androlog.reporter.Reporter#send(android.content.Context, java.lang.String, java.lang.Throwable)
     */
    @Override
    public boolean send(Context context, String mes, Throwable err) {
        if (url != null) {
            String report = new Report(context, mes, err).getReportAsJSON().toString();
            try {
                post(url, "report=" + report);
                return true;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    /**
     * Executes the given request as a HTTP POST action.
     * @param url the url
     * @param params the parameter
     * @return the response as a String.
     * @throws IOException if the server cannot be reached
     */
    public static void post(URL url, String params) throws IOException {
        URLConnection conn = url.openConnection();
        OutputStreamWriter writer = null;
        System.out.println("Posting report to " + url.toExternalForm());
        try {
            conn.setDoOutput(true);
            writer = new OutputStreamWriter(conn.getOutputStream(),
                    Charset.forName("UTF-8"));
            //write parameters
            writer.write(params);
            writer.flush();
            System.out.println(read(conn.getInputStream()));
        } finally {
            if (writer != null) {
                writer.close();
            }
        }
    }

    /**
     * Reads an input stream and returns the result as a String.
     *
     * @param in
     *            the input stream
     * @return the read String
     * @throws IOException
     *             if the stream cannot be read.
     */
    public static String read(InputStream in) throws IOException {
        InputStreamReader isReader = new InputStreamReader(in, "UTF-8");
        BufferedReader reader = new BufferedReader(isReader);
        StringBuffer out = new StringBuffer();
        char[] c = new char[4096];
        for (int n; (n = reader.read(c)) != -1;) {
            out.append(new String(c, 0, n));
        }
        reader.close();

        return out.toString();
    }


}
