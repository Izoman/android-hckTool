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


        import java.util.ArrayList;
        import java.util.Map;

public final class Lists {
    private Lists() {}

    public static final <T> ArrayList<T> singleton(T x) {
        ArrayList<T> result = new ArrayList<T>();
        result.add(x);
        return result;
    }

    public static final String map(Map<Character, Character> map, String in) {
        StringBuilder result = new StringBuilder(in.length());
        for(char c : in.toCharArray()) {
            Character ud = map.get(c);
            if(ud == null) ud = c;
            result.append(ud);
        }
        return result.toString();
    }
}
