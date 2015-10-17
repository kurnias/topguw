package gsm.tools;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Link for kalibrate-rtl tool and java
 *
 * @author bastien enjalbert
 */
public class Kalibrate {

    /**
     * base regex for a kalibrate output group 1 nothing group 2 cell fequency
     * group 3 freq type (- or +) group 4 freq correction group 5 power
     */
    public static Pattern RGX_KAL
            = Pattern.compile(".*chan: [0-9]* \\(([0-9]*.[0-9]*)MHz (-+) ([0-9]*.[0-9]*)kHz\\)	power: ([0-9]*.[0-9]*)");

    /**
     * Start kalibrate-rtl (kal) to get GSM tower
     *
     * @param whichGsm GSM type (900, 1800, ..)
     * @param gain
     * @return an arraylist containing GSM tower detected by kal index 1 : freq
     * (corrected) index 2 : power
     * @throws Exception if RTL-SDR device is not plugged
     */
    public static ArrayList<String[]> getGsmCell(String whichGsm, String gain) throws Exception {
        ArrayList<String[]> gsmCells = new ArrayList<String[]>();

        ProcessBuilder pb = new ProcessBuilder("kal", "-s", whichGsm, "-g", gain);
        pb.redirectErrorStream(true);
        Process p = pb.start();

        p.getOutputStream().flush();
        BufferedReader reader = new BufferedReader(new InputStreamReader(p.getInputStream()));
        String ligne = new String();
        while ((ligne = reader.readLine()) != null) {
            System.out.println("debug : " + ligne); // TODO : delete (DEBUG LINE)
            if (ligne.equals("No supported devices found.")) {
                throw new Exception("Please plug-in your RTL-SDR device.");
            }
            Matcher m = RGX_KAL.matcher(ligne);
            if (m.matches()) {
                // add the correct frequency					
                String[] temp = new String[2];
                BigDecimal add = null;
                if (m.group(2).equals("+")) {
                    add = new BigDecimal(Double.parseDouble(m.group(3)));
                } else {
                    add = new BigDecimal(-(Double.parseDouble(m.group(3))));
                }
                BigDecimal big = new BigDecimal(Double.parseDouble(m.group(1)) * 1000000);
                big = big.add(add);
                //System.out.println("detected frequency : " + Double.toString(Double.parseDouble(m.group(1))*1000000));
                System.out.println(Long.toString(big.longValue()));
                temp[0] = Long.toString(big.longValue());
                temp[1] = m.group(4).toString();
                gsmCells.add(temp);
            }
        }
        p.destroy();
        p.destroyForcibly();
	// assert p.getInputStream().read() == -1;
        gsmCells.sort(null);
        return gsmCells;
    }

}
