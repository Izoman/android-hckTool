package com.izoman.hcktool.helpers;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import android.util.Log;

public class RootAccess {

	private static final String TAG = "RootAccess";

	public static boolean isGranted() {
		Process process = null;
		DataOutputStream os = null;
		InputStreamReader osRes = null;
		boolean hasRoot = false;

		try {
			process = Runtime.getRuntime().exec("su");
			os = new DataOutputStream(process.getOutputStream());
			osRes = new InputStreamReader(process.getInputStream());
			BufferedReader reader = new BufferedReader(osRes);

			os.writeBytes("id" + "\n");
			os.flush();

			os.writeBytes("exit \n");
			os.flush();

			String line = reader.readLine();
			while (line != null) {
				Set<String> ID = new HashSet<String>(Arrays.asList(line.split(" ")));
				for (String id : ID) {
					if (id.toLowerCase().contains("uid=0")) {
						hasRoot = true;
						break;
					}
				}
				line = reader.readLine();
			}
			process.waitFor();
		} catch (InterruptedException e) {
			Log.e(TAG, "error checking root access", e);
		} catch (IOException e) {
			Log.e(TAG, "error checking root access", e);
		} finally {
			try {
				if (os != null) {
					os.close();
				}
				if (osRes != null) {
					osRes.close();
				}
			} catch (IOException e) {
				// swallow error
			} finally {
				if (process != null)
					process.destroy();
			}
		}
		return hasRoot;
	}
	
}
