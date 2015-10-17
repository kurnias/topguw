/*
 * Dedicated.java                                     ENJALBERT BASTIEN
 * 21 Jun. 2015
 */
package gsm.tools;

import gsm.gui.Principal;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.regex.Matcher;


/**
 * Dedicated channel management for GSM 
 * @author Enjalbert Bastien
 */
@SuppressWarnings("serial")
public class Dedicated extends Principal{
	
	public static void main(String[] args)  {
		try {
			General.getAirprobeOutput(new File("/media/SAUVEGARDE/CFILE/rdz.12.juin.big.cfile"));
		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		System.out.println("RESULTAT GETBURST FROM FN : ");
		for(String s : getBurstsFromFn("1218535")) 
			System.out.println(s);
		
	    
	
	}
	
	/**
	 * Convert ArrayList<String> into ArrayList<String[]> (split with spaces)
	 * @param inLine 
	 * @return splitted line into an ArrayList
	 */
	public static ArrayList<String[]> lignesToTab(ArrayList<String> inLine) {
	
		ArrayList<String[]> framesEnTab = new ArrayList<String[]>();
		for(int i = 0 ; i < inLine.size() ; i++) {
			framesEnTab.add(ligneToTab(inLine.get(i)));
		}
		return framesEnTab;
	}
	
	/**
	 * Convert a String into an array (split with spaces)
	 * @param line the line to split
	 * @return the splitted line into an array
	 */
	public static String[] ligneToTab(String ligne) {
		
		/* Array are like this (with dedicated channel [ S config with airprobe])
		 * 
		 * Index    | Contents
		 * 0		| Burst Type (i.e "C1", "P0", "S0", ...)
		 * 1        | fn 
		 * 2        | fn (a5/1) + ":"
		 * 3        | data (one burst)
		 */
		
		/*
		 * Index    | Contents
		 * 0		| fn
		 * 1        | number + ":" 
		 * 2-25     | hex frame 
		 */
		
		String[] splitArray = null ; 
		splitArray = ligne.split(" ");

		return splitArray;
	}

	/**
	 * Search for SI type 5/5ter/6
	 * @param inArray frames in an array
	 * @return 
	 */
	public static ArrayList<String[]> getSysInfo(ArrayList<String[]> inArray){
		ArrayList<String[]> si = new ArrayList<String[]>();
		
		boolean si5getted = false;
		boolean si6getted = false;
		boolean si5tergetted = false;
		
		for(int i = 0 ; i < inArray.size() ; i++) {
			if(inArray.get(i).length == 25) {
				// SI5 
				if(si5getted == false && inArray.get(i)[7].equals("06") 
						&& inArray.get(i)[8].equals("1d")) {
					String[] temp = new String[3];
					//fn
					temp[0] = inArray.get(i)[0];
					//hex value
					temp[1] = "";
					for(int a = 2 ; a < 25 ; a++) 
					temp[1] += inArray.get(i)[a];
					//si type
					temp[2] = "SI5";
					si5getted = true;
					si.add(temp);
				}
				// SI5TER
				if(si5tergetted == false && inArray.get(i)[7].equals("06") 
						&& inArray.get(i)[8].equals("06")) {
					String[] temp = new String[3];
					//fn
					temp[0] = inArray.get(i)[0];
					//hex value
					temp[1] = "";
					for(int a = 2 ; a < 25 ; a++) 
					temp[1] += inArray.get(i)[a];
					//si type
					temp[2] = "SI5TER";
					si5tergetted = true;
					si.add(temp);
				}
				// SI6
				if(si6getted == false && inArray.get(i)[7].equals("06") 
						&& inArray.get(i)[8].equals("1e")) {
					String[] temp = new String[3];
					//fn
					temp[0] = inArray.get(i)[0];
					//hex value
					temp[1] = "";
					for(int a = 2 ; a < 25 ; a++) 
					temp[1] += inArray.get(i)[a];
					//si type
					temp[2] = "SI6";
					si6getted = true;
					si.add(temp);
				}
				if(si5getted == true && si6getted == true && si5tergetted == true) {
					break;
				}
			}
		}
		
		// intialize object
		systemInfo = si;
		/*
		 * ind 0 : fn
		 * ind 1 : hex value
		 * ind 3 : SI type
		 */
		return si;
	}
	
	
	/**
	 * Search for Ciphering Mode Command
	 * @param inArray frames in an array
	 * @return 
	 */
	public static ArrayList<String[]> getCipherModCmd(ArrayList<String[]> inArray){
		ArrayList<String[]> ciphModCmd = new ArrayList<String[]>();
		
		for(int i = 0 ; i < inArray.size() ; i++) {
			if(inArray.get(i).length == 25) {
				// 
				if(inArray.get(i)[5].equals("06") 
						&& inArray.get(i)[6].equals("35")) {
					String[] temp = new String[2];
					//fn
					temp[0] = inArray.get(i)[0];
					//hex value
					temp[1] = "";
					for(int a = 2 ; a < 25 ; a++) 
					temp[1] += inArray.get(i)[a];
					ciphModCmd.add(temp);
				}
			}
		}
		
		cipherModCommand = ciphModCmd;
		
		/*
		 * ind 0 : fn
		 * ind 1 : hex value
		 */
		return ciphModCmd;
	}
	
	/** 
	 * Search for potential SI position after a ciphering mode command
	 * @return potential position
	 */
	public static ArrayList<String[]> getEncryptedSi() throws Exception {

		int localDedicatedChannelFn = 0;
		
		ArrayList<String[]> cipheredSi = new ArrayList<String[]>();
		// TODO : mettre le bouton grisé tant que les deux test d'avant n'ont pas été lancé
		// mieux niveau IHM
		if(systemInfo == null || cipherModCommand == null) {
			throw new Exception(START_LINE + "Error : you have to find SI cleartext and Ciphering Mode Command position before.\n");
		} else {
			for(int i = 0 ; i < systemInfo.size() ; i++)	{
				for(int j = 0 ; j < dedicatedChannelTab.size() ; j++) {
					if(dedicatedChannelTab.get(j).length == 4 &&
							isInteger(dedicatedChannelTab.get(j)[1]) && 
							Integer.parseInt(dedicatedChannelTab.get(j)[1]) > // possible si encrypted is after unencryted si
									Integer.parseInt(systemInfo.get(i)[0]) &&
							(Integer.parseInt(dedicatedChannelTab.get(j)[1])
									% 102 == 32
									|| Integer.parseInt(dedicatedChannelTab.get(j)[1])
									% 102 == 36
									|| Integer.parseInt(dedicatedChannelTab.get(j)[1])
									% 102 == 40
									|| Integer.parseInt(dedicatedChannelTab.get(j)[1])
									% 102 == 44)) {
						for(int a = 0 ; a < cipherModCommand.size() ; a++) {
							// if the possible SI ciphered is after the Ciphering Mode Command : 
							if(Integer.parseInt(cipherModCommand.get(a)[0]) < Integer.parseInt(dedicatedChannelTab.get(j)[1])
								&& localDedicatedChannelFn != Integer.parseInt(dedicatedChannelTab.get(j)[1])
								&& isParityErr(dedicatedChannelTab.get(j)[1])) {
								String[] temp = new String[4];
								/*
								 * ind 0 : SI plaintext Frame Number
								 * ind 1 : SI plaintext Frame Number % 102
								 * ind 2 : SI Ciphered possible position
								 * ind 3 : SI Ciphered possible position % 102 -
								 */
								
								localDedicatedChannelFn = Integer.parseInt(dedicatedChannelTab.get(j)[1]);
								temp[0] = systemInfo.get(i)[0];
								temp[1] = "fn[" + Integer.toString(Integer.parseInt(systemInfo.get(i)[0]) % 102) + "]";
								temp[2] = dedicatedChannelTab.get(j)[1];
								temp[3] = "fn[" + Integer.toString(Integer.parseInt(dedicatedChannelTab.get(j)[1]) % 102) + "]";
								cipheredSi.add(temp);
							}
						}
					}
				}
			}
		}
		
		return cipheredSi;
	}
	
	/**
	 * Check if a frame number is link to an encrypted SI frame (after founding them)
	 * @param fn the frame number 
	 * @param fn2 the second frame number
	 * @return true if the frame number is link, false if not
	 */
	public static boolean isLinkToThisSI(String fn, String fn2) {
		for(int i = 0 ; i < encryptedSiPosition.size() ; i++) {
			if(encryptedSiPosition.get(i)[0].equals(fn)
					&& encryptedSiPosition.get(i)[2].equals(fn2)) {
						return true;
					}
		}
			return false;
	}
	
	/**
	 * Check if a frame number is linked to a parity error (cannot decode)
	 * @param fn the frame number
	 * @return true if the frame seems unable to be decoded by airprobe
	 */
	public static boolean isParityErr(String fn) {
		
		ArrayList<String> temp = General.readFile(file.getAbsolutePath() + "_" + timeslot + "S");
		
		for(int i = 0 ; i < temp.size() ; i++) {
			if(General.RGX_CONVDEC_ERR.matcher(temp.get(i)).matches()) {
				Matcher recup_err = General.RGX_CONVDEC_ERR.matcher(temp.get(i)); 
				if(recup_err.find()) {
					if(recup_err.group(1).equals(fn));
						return true;
					} else { 
						return false;
					}
			}
		}
		
		return false;
	}

	/**
	 * Get Bursts from a frame number
	 * @param fn the frame number
	 * @return an array with at least one burst from the frame number (if other bursts are missing in the dump), else 4 bursts are returned
	 */
	public static String[] getBurstsFromFn(String fn) {
		
		String[] bursts = new String[4];
		for(int i = 0 ; i < 4 ; i++) {
			bursts[i] = "no exist";
		}
		int integerFn = Integer.parseInt(fn);
		
		boolean finish = false;
		
		for(int i = 0 ; finish == false && i < dedicatedChannelTab.size(); i++) {
			String[] line = dedicatedChannelTab.get(i);
			
			if(line.length == 4 && isInteger(line[1]) && line[0].charAt(0) == 'C') {
				if(Integer.parseInt(line[1]) == integerFn-3) {
					bursts[0] = line[3];
				} else if(Integer.parseInt(line[1]) == integerFn-2) {
					bursts[1] = line[3];
				} else if(Integer.parseInt(line[1]) == integerFn-1) {
					bursts[2] = line[3];
				} else if(Integer.parseInt(line[1]) == integerFn) {
					bursts[3] = line[3];
					finish = true;
				} else {
					// DO NOTHING
				}
			}
		}
		return bursts;
	}
	
	/**
	 * Return bursts from a hexa frame without time advance
	 * @param hexaFrame frame in hexadecimal
	 * @param fn the frame number 
	 * @param siType the System Information type (5/5ter/6)
	 * @return String[] with all 4 bursts from the frame
	 * @throws IOException if error while executing command
	 */
	public static String[] getBursts(String hexaFrame, String fn, String siType) throws IOException {
		String[] bursts = new String[6];
		bursts[4] = fn;
		bursts[5] = siType;
		int i = 0;
	    
	    // delete Time Advance 
		StringBuilder hexaFrameNoTA = new StringBuilder(hexaFrame);
		hexaFrameNoTA.setCharAt(2, '0');
		hexaFrameNoTA.setCharAt(3, '0');
		ProcessBuilder pb = new ProcessBuilder("./gsmframecoder", hexaFrameNoTA.toString());
		pb.redirectErrorStream(true);
		pb.directory(gsmFrameCoder);
		Process p = pb.start();

		p.getOutputStream().flush();
		BufferedReader reader = new BufferedReader (new InputStreamReader(p.getInputStream()));
		String ligne = new String();
		while ((ligne = reader.readLine ()) != null) {
			if(ligne.length() == 114 && i < 4) {
				bursts[i] = ligne;
				i++;
			}
		}
		p.destroy();
		p.destroyForcibly();
		return bursts;
	}
	
	
	/**
	 * Get : (fn % 102 == {32,47})
	 * @param enTableau frames in an arraylist of string[]
	 * @return Une arraylist de int[] composé des fn possibles et de leurs résultats % 102
	 * @obselete
	 * @unused
	 */
	// TODO : delete
	public static ArrayList<String[]> findSysInfo(ArrayList<String[]> enTableau) {
		ArrayList<String[]> lesSi = new ArrayList<String[]>();
		String tempFn = "";
		
		/*
		 * INDEX 0 : FRAME NUMBER
		 * INDEX 1 : FRAME NUMBER MODULO 102
		 */
		
		for(int i = 0 ; i < enTableau.size() ; i++) {
			String[] temp = new String[2];
			// Si fn % 102 == 32,47 -> on ajoute 
			if(enTableau.get(i).length > 2 && isInteger(enTableau.get(i)[1]) 
					&& (Integer.parseInt(enTableau.get(i)[1]) % 102 >= 32 
					&&	Integer.parseInt(enTableau.get(i)[1]) % 102 <= 47)
					&& !(tempFn.equals(enTableau.get(i)[1]))) {
				tempFn = enTableau.get(i)[1];
				temp[0] = enTableau.get(i)[1];
				if(Integer.parseInt(enTableau.get(i)[1]) % 102 == 35 
						|| Integer.parseInt(enTableau.get(i)[1]) % 102 == 39
						|| Integer.parseInt(enTableau.get(i)[1]) % 102 == 43
						|| Integer.parseInt(enTableau.get(i)[1]) % 102 == 47) {
					temp[1] = String.valueOf(Integer.parseInt(enTableau.get(i)[1]) % 102) + "\nFound end of frame";
				} else {
					temp[1] = String.valueOf(Integer.parseInt(enTableau.get(i)[1]) % 102);
				}
				
				lesSi.add(temp);
			}
		}
		return lesSi;
	}
	
	/**
	 * Detects whether a character string is formatted as a number
	 * @param str the string to test
	 * @return true if the string can be parsed into an interger, false if not
	 */
	public static boolean isInteger(String str) {
		if (str == null) {
			return false;
		}
		int length = str.length();
		if (length == 0) {
			return false;
		}
		int i = 0;
		if (str.charAt(0) == '-') {
			if (length == 1) {
				return false;
			}
			i = 1;
		}
		for (; i < length; i++) {
			char c = str.charAt(i);
			if (c <= '/' || c >= ':') {
				return false;
			}
		}
		return true;
	}

}