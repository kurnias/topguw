/*
 * Broadcast.java                                     ENJALBERT BASTIEN
 * 19 Jun. 2015
 */
package gsm.tools;

import java.util.ArrayList;

/**
 * Broadcast channel management for GSM
 *
 * @author Enjalbert Bastien
 */
public class Broadcast {

    /**
     * Convert ArrayList of String into ArrayList of String[] (split with
     * spaces)
     * @param inLine
     * @return splitted line into an ArrayList
     */
    public static ArrayList<String[]> lignesToTab(ArrayList<String> inLine) {
        ArrayList<String[]> framesEnTab = new ArrayList<>();
        for (int i = 0; i < inLine.size(); i++) {
            framesEnTab.add(ligneToTab(inLine.get(i)));
        }
        return framesEnTab;
    }

    /**
     * Convert a String into an array (split with spaces)
     *
     * @param line the line to split
     * @return the splitted line into an array
     */
    public static String[] ligneToTab(String line) {

        /* Le tableau de sorti est organisé de cette manière
         * 
         * Indice   | Contenu
         * 0		| Frame Number (fn)
         * 1        | "0:"
         * 2-24     | data
         */
        String[] splitArray;
        splitArray = line.split(" ");

        /* --------- TEST AFFICHAGE DU TABLEAU -----------
         for(int i = 0; i< splitArray.length;i++){
         // On affiche chaque élément du tableau
         System.out.println("élement n° " + i + "=[" + splitArray[i]+"]");
         }*/
        return splitArray;
    }

    /**
     * Extract time slot used for an Immediate Assignment (frame has to be
     * splitted before)
     *
     * @param frame the splitted frame
     * @return timeslot and configuration
     */
    public static ArrayList<String> extractTsConf(String[] frame) {
        ArrayList<String> sorti = new ArrayList<>();
        // We get binary information about the frame
        String info = General.hexToBin(frame[6]);
        // Check integrity of the binary frame
        for (; info.length() < 8;) {
            info = "0" + info;
        }
        // Get timeslot
        String timeslot = String.valueOf(Integer.parseInt(info.substring(5, 8), 2));
        // We get timeslot and which configuration is used
        if (info.charAt(1) == '1') { // If SDCCH/8 + SACCH/C8 or CBCH
            sorti.add(timeslot);
            sorti.add("1");
        } else {
            sorti.add(timeslot);
            sorti.add("0");
        }
        return sorti;
    }

    /**
     * Test if a frame is an Immediate Assignment (0x063f) or not
     *
     * @param frame the splitted frame
     * @return true if the passed frame is an IA, false otherwise
     */
    public static boolean isImmediateAssignment(String[] frame) {

        return frame.length > 4 && frame[3].equals("06") && frame[4].equals("3f");
    }

    /**
     * Looking for "Immediate Assignment"
     *
     * @param frames all splitted frames into an arraylist
     * @return an array of immediate assignment
     */
    public static ArrayList<Integer> extractImAs(ArrayList<String[]> frames) {
        ArrayList<Integer> immediateAssignment = new ArrayList<>();

        for (int i = 0; i < frames.size(); i++) {
            // we check the i th frame
            if (isImmediateAssignment(frames.get(i))) {
                immediateAssignment.add(i);
            }
        }
        return immediateAssignment;
    }

}
