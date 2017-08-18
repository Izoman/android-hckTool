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

/**
 * <p>
 * Application JNI interfaces.
 * </p>
 * <p>
 * Also used to force Google Play to filter by CPUs that this application actually supports.
 * </p>
 * 
 * @author Will Shackleton <will@digitalsquid.co.uk>
 *
 */
public class JNI {
    static {
        System.loadLibrary("netspooflib");
    }
    
    /**
     * Chmod glibc function setting a file to 0755
     * @param filename
     * @return 1 on success, 0 on failure
     */
    public static native int setExecutable(String filename);
}
