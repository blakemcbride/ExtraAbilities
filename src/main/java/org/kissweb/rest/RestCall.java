package org.kissweb.rest;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;

/**
 * Author: Blake McBride
 * Date: 4/5/20
 */
public class RestCall {

    /**
     *
     * @param urlStr
     * @param contentType
     * @param authorization
     * @param outStr
     * @return
     * @throws IOException
     */
    public static String getService(String urlStr, String contentType, String authorization, String outStr) throws IOException {
        HttpURLConnection con = null;
        OutputStreamWriter out = null;
        BufferedReader in = null;
        String res = "";

        try {
            URL url = new URL(urlStr);
            con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setRequestProperty("Content-type", contentType);
            con.setRequestProperty("cache-control", "no-cache");
            if (authorization != null)
                con.setRequestProperty("authorization", authorization);
            con.setDoInput(true);

            if (outStr != null &&  outStr.length() > 0) {
                con.setDoOutput(true);
                out = new OutputStreamWriter(con.getOutputStream());
                out.write(outStr);
                out.flush();
            }
            in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            String line;
            while ((line = in.readLine()) != null) {
                res = res.concat(line);
            }
        } finally {
            if (out != null)
                try {
                    out.close();
                } catch (Exception e) {
                    // ignore
                }
            if (in != null)
                try {
                    in.close();
                } catch (Exception e) {
                    // ignore
                }
            if (con != null)
                con.disconnect();
        }
        //       Files.write(Paths.get("abc.xml"), res.getBytes(), StandardOpenOption.CREATE, StandardOpenOption.WRITE, StandardOpenOption.APPEND);
        return res;
    }

}
