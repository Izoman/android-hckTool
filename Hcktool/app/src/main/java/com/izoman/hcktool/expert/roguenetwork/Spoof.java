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


        import android.app.Dialog;
        import android.content.Context;
        import android.content.Intent;
        import android.support.annotation.NonNull;

        import java.io.Serializable;
        import java.util.ArrayList;
        import java.util.HashMap;
        import java.util.Map;


public abstract class Spoof implements Serializable, Comparable<Spoof> {
    private static final long serialVersionUID = -3207729013734241941L;

    private  String title, description;

    public Spoof(String title, String description) {
        this.description = description;
        this.title = title;
    }

    public  Spoof(){}

    /**
     * Reinitialises this spoof after it has been deserialised
     * @param context
     */
    public void transientInit(Context context) { }

    public Dialog displayExtraDialog(Context context, OnExtraDialogDoneListener onDone) {
        return null;
    }

    public Intent activityForResult(Context context) {
        return null;
    }
    /**
     * A second activity to be displayed afterwards.
     * @param context
     * @return
     */
    public Intent activityForResult2(Context context){ return null; }

    /**
     *
     * @param result
     * @return <code>true</code> to continue the process.
     */
    public boolean activityFinished(Context context, Intent result) { return true; }
    /**
     *
     * @param result
     * @return <code>true</code> to continue the process.
     */
    public boolean activityFinished2(Context context, Intent result) { return true; }

    public String getDescription() {
        return description;
    }

    public String getTitle() {
        return title;
    }

    public static interface OnExtraDialogDoneListener {
        void onDone();
    }

    public static interface OnActivityResultListener {
        void onResult(Intent result);
    }

    @Deprecated
    public Map<String, String> getCustomEnv() {return new HashMap<String, String>();}

    @Override
    public int compareTo(@NonNull Spoof other) {
        return getTitle().compareTo(other.getTitle());
    }

    @Override
    public String toString() {
        return title;
    }
    // TODO: Make abstract
    public abstract void modifyRequest( HttpRequest request);
    public abstract void modifyResponse( HttpResponse response, HttpRequest request);

    /**
     * This function expands and groups spoofs into their simplest form.
     * Currently this involves:
     * Grouping {@link HtmlEditorSpoof} objects into a {@link HtmlSpoof}.
     * @param spoof
     * @return A list of expanded spoofs, or a singleton of spoof.
     */
    public static ArrayList<Spoof> expandSpoof(Spoof spoof) {
                    ArrayList<HtmlEditorSpoof> htmlSpoofs = new ArrayList<HtmlEditorSpoof>();
            ArrayList<Spoof> otherSpoofs = new ArrayList<Spoof>();

//        if(spoof instanceof MultiSpoof) {
//            ArrayList<Spoof> spoofs = ((MultiSpoof)spoof).getSpoofs();
//
//            // Sort into a list of ImageSpoof and a list of others
//            ArrayList<ImageSpoof> imageSpoofs = new ArrayList<ImageSpoof>();
//            ArrayList<HtmlEditorSpoof> htmlSpoofs = new ArrayList<HtmlEditorSpoof>();
//            ArrayList<Spoof> otherSpoofs = new ArrayList<Spoof>();
//            for(Spoof s : spoofs) {
//                if(s instanceof ImageSpoof)
//                    imageSpoofs.add((ImageSpoof)s);
//                else if(s instanceof HtmlEditorSpoof)
//                    htmlSpoofs.add((HtmlEditorSpoof)s);
//                else otherSpoofs.add(s);
//            }
//
//            // Fold ImageSpoofs up
//            if(imageSpoofs.size() > 0) {
//                ImageSpoof first = imageSpoofs.remove(0);
//                for(ImageSpoof next : imageSpoofs) {
//                    first.mergeImageSpoof(next);
//                }
//                otherSpoofs.add(first);
//            }

            // Group HtmlEditorSpoofs up
            if(htmlSpoofs.size() > 0) {
                HtmlSpoof group = new HtmlSpoof();
                for(HtmlEditorSpoof s : htmlSpoofs)
                    group.addEditor(s);
                otherSpoofs.add(group);

            return otherSpoofs;
        } else if(spoof instanceof HtmlEditorSpoof) {
            return Lists.singleton((Spoof)new HtmlSpoof((HtmlEditorSpoof) spoof));
        } else {
            return Lists.singleton(spoof);
        }
    }
}
