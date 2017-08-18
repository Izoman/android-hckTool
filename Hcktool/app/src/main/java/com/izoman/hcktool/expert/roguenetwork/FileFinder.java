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

package com.izoman.hcktool.expert.roguenetwork;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.util.Log;

import java.io.File;
import java.io.FileNotFoundException;

public final class FileFinder implements LogConf {
    private FileFinder() { }
    private static boolean initialised = false;
    
    private static Context context;
    
    public static String SU = "";
    public static String IPTABLES = "";

    private static final String[] IPTABLES_PATHS = { "/system/bin/iptables", "/system/xbin/iptables", "/system/sbin/iptables", "/vendor/bin/iptables", "iptables" };
    private static final String[] SU_PATHS = { "/system/bin/su", "/system/xbin/su", "/system/sbin/su", "/vendor/bin/su", "su" };
    
    /**
     * Searches for the iptables executable
     * @return
     */
    private static String findIptables(boolean useLocal, SharedPreferences prefs) {
        if(useLocal && prefs != null) {
            if(prefs.getBoolean("builtiniptables", true)) {
                Log.i(TAG, "Using local copy of iptables");
                return FileInstaller.getScriptPath(context, "iptables");
            }
            String customPath = prefs.getString("pathToIptables", "");
            if(!customPath.equals("") && new File(customPath).exists()) return customPath;
        }
        for(String bb : IPTABLES_PATHS) {
            if(new File(bb).exists()) {
                return bb;
            }
        }
        return "";
    }
    
    
    /**
     * Searches for the su executable
     * @return
     */
    private static String findSu(SharedPreferences prefs) {
        if(prefs != null) {
            String customPath = prefs.getString("pathToSu", "");
            if(!customPath.equals("") && new File(customPath).exists()) return customPath;
        }
        for(String su : SU_PATHS) {
            if(new File(su).exists()) {
                return su;
            }
        }
        return "";
    }
    
    public static final void initialise(Context appContext) throws FileNotFoundException {
        FileFinder.context = appContext;
        if(initialised) {
            return;
        }
        initialised = true;
        loadPaths();
    }
    
    /**
     * (re)loads the SU and BB paths, perhaps after a preference change.
     * @throws FileNotFoundException 
     */
    public static final void loadPaths() throws FileNotFoundException {
        SharedPreferences prefs = null;
        if(context != null) {
            prefs = PreferenceManager.getDefaultSharedPreferences(context);
        }
        SU = findSu(prefs);
        if(SU.equals("")) {
            throw new FileNotFoundException("su");
        }
        
        IPTABLES = findIptables(true, prefs);
        if(IPTABLES.equals("")) {
            throw new FileNotFoundException("iptables");
        }
    }

    /**
     * Initialise that doesn't search custom paths. Not recommended.
     * @throws FileNotFoundException
     */
    public static final void initialise() throws FileNotFoundException {
        if(initialised) {
            return;
        }
        initialised = true;

        SU = findSu(null);
        if(SU.equals("")) {
            throw new FileNotFoundException("su");
        }
    }
}
