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
import android.util.Log;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Manages the lifecycle of a spoof, including running the iptables and arpspoof.
 * @author Will Shackleton <will@digitalsquid.co.uk>
 *
 */
public class RunManager implements LogConf {
    private final Context context;
    public final HardwareConfig config;
    
    public RunManager(Context context, HardwareConfig config) {
        this.context = context;
        this.config = config;
    }
    
    public ArrayList<Spoof> getSpoofList() {
        ArrayList<Spoof> spoofs = new ArrayList<Spoof>();
        
//        spoofs.add(new ImageSpoof(context, ImageSpoof.IMAGE_FLIP));
//        spoofs.add(new ImageSpoof(context, ImageSpoof.IMAGE_WOBBLY));
//        spoofs.add(new CustomGalleryImageChange(context,
//                CustomGalleryImageChange.MODE_TROLLFACE));
//        spoofs.add(new CustomGalleryImageChange(context,
//                CustomGalleryImageChange.MODE_CUSTOM));
//        spoofs.add(new VideoChange(context, true));
//        spoofs.add(new VideoChange(context, false));
//
//        spoofs.add(new CustomTextChange(context));
        
//        spoofs.add(new RedirectSpoof(context, RedirectSpoof.MODE_BLUEBALL));
        spoofs.add(new RedirectSpoof());

//        spoofs.add(new TitleChange(context, TitleChange.MODE_FLIP));
//        spoofs.add(new TitleChange(context, TitleChange.MODE_REVERSE));
//
//        spoofs.add(new ContentChange(context, ContentChange.MODE_FLIP));
//        spoofs.add(new ContentChange(context, ContentChange.MODE_GRAVITY));
//        spoofs.add(new ContentChange(context, ContentChange.MODE_DELETE));
//
        Collections.sort(spoofs);
//
//        spoofs.add(0, new MultiSpoof());
//        spoofs.add(new NullSpoof());
        
        return spoofs;
    }
    
    public boolean isSpoofRunning() {
        return spoofRunning;
    }

    private boolean spoofRunning = false;
    private final Object spoofLock = new Object();
    
    Process su;
    BufferedReader cout;
    BufferedReader cerr;
    OutputStreamWriter cin;
    
    NSProxy proxy;
    
    public void startSpoof(SpoofData spoof) throws IOException {
        if(spoofRunning) throw new IllegalStateException("Spoof already running");
        FileFinder.initialise(context.getApplicationContext()); // In case of weird android instancing
        synchronized(spoofLock) {
            spoofRunning = true;
            
            final String router = spoof.getRouterIpString();
            final String victim = spoof.getVictimString();
            final String spoofParams = String.format("%s %s", victim, router);
            
            ProcessBuilder pb = new ProcessBuilder(FileFinder.SU, "-c",
                    FileInstaller.getScriptPath(context, "spoof") + " " + FileInstaller.getScriptPath(context, "config") + " " +
                    spoofParams); // Pass config script as arg.
            
            Log.d(TAG, "Command: " + pb.command());
            
            // We now write the env to a config file, which is loaded in.
            Map<String, String> env = new HashMap<String, String>();
            
            if(!spoof.isRunningPassively()) {
                env.put("WLAN", spoof.getMyIface());
                env.put("IP", spoof.getMyIp().getHostAddress());
                env.put("SUBNET", spoof.getMySubnetBaseAddressString());
                env.put("MASK", spoof.getMySubnetString());
                env.put("SHORTMASK", String.valueOf(spoof.getMySubnet()));
            }
            
            env.put("ARPSPOOF", FileInstaller.getScriptPath(context, "arpspoof"));
            env.put("IPTABLES", FileFinder.IPTABLES);

            ProcessRunner.writeEnvConfigFile(context, env);
            
            // Start proxy
            proxy = new NSProxy(context, spoof.getSpoofs());
            proxy.start();

            su = pb.start();
            cout = new BufferedReader(new InputStreamReader(su.getInputStream()));
            cerr = new BufferedReader(new InputStreamReader(su.getErrorStream()));
            cin  = new OutputStreamWriter(su.getOutputStream());
        }
    }
    
    /**
     * Stops the current spoof.
     * @return The final list of output messages
     * @throws IOException
     */
    public void stopSpoof(SpoofData spoof) throws IOException {
        if(!spoofRunning) return; // Don't do anything.
        synchronized(spoofLock) {
            if (cin != null) {
                cin.write("\n");
                cin.flush();
            }
            
            try {
                if (su != null) {
                    su.waitFor();
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            
            if(proxy != null) {
                Log.v(TAG, "Closing proxy");
                proxy.stop();
                proxy = null;
            }
        }
    }
    
    /**
     * Stops the current spoof.
     * @return The final list of output messages
     * @throws IOException
     */
    public ArrayList<String> finishStopSpoof() throws IOException {
        synchronized(spoofLock) {
            ArrayList<String> finalOutput = getNewSpoofOutput();
            
            try {
                cin.close();
                cout.close();
                cerr.close();
            } catch (IOException e) {
            }
            
            cin = null;
            cout = null;
            cerr = null;
            su = null;
            
            spoofRunning = false;
            
            return finalOutput;
        }
    }
    
    
    /**
     * Checks if the process is stopped. Doesn't actually close anything, though.
     * stopSpoof must be called with onlyClosePipes true if this returns true.
     * @return
     */
    public boolean checkIfStopped() {
        if(!spoofRunning) return false;
            
        if(su == null) return false;
        try {
            su.exitValue();
        } catch (IllegalThreadStateException e) {
            return false;
        }
        return true;
    }
    
    public synchronized ArrayList<String> getNewSpoofOutput() throws IOException {
        ArrayList<String> items = new ArrayList<String>();
        
        while(cerr != null && cerr.ready()) {
            String line = cerr.readLine();
            Log.v(TAG, "cerr: " + line);
            items.add(line);
        }
        while(cout != null && cout.ready()) {
            String line = cout.readLine();
            Log.v(TAG, "cout: " + line);
            items.add(line);
        }
        return items;
    }
}
