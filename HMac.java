import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class HMac {

	// Calcule le hmac à partir du document
	public static byte[] CalculeHmac(byte[] cle, String fileIn)
			throws NoSuchAlgorithmException, FileNotFoundException {

		byte[] buffer, resume;
		MessageDigest fonction_de_hachage;
		fonction_de_hachage = MessageDigest.getInstance("SHA1");

		File fichier = new File(fileIn);
		FileInputStream fis = new FileInputStream(fichier);
		buffer = new byte[8192];
		int nbOctetsLus = 0;

		fonction_de_hachage.update(cle, 0, (int) cle.length);

		// lis le fichier et rempli la fonction de hashage
		try {
			nbOctetsLus = fis.read(buffer);
		} catch (IOException e) {
			e.printStackTrace();
		}

		while (nbOctetsLus != -1) {
			fonction_de_hachage.update(buffer, 0, nbOctetsLus);
			try {
				nbOctetsLus = fis.read(buffer);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		try {
			fis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		// digere
		resume = fonction_de_hachage.digest();

		return resume;
	}

	// Insère la clef dans le doc sur la 2e ligne
	public static void InsereHmac(String comHmac, String fileIn, String fileOut)
			throws IOException {

		FileInputStream fis = new FileInputStream(fileIn);

		FileOutputStream writer = new FileOutputStream(fileOut);

		int c;

		while ((c = fis.read()) != '\n') {
			writer.write(c);
		}
		writer.write('\n');

		writer.write(comHmac.getBytes());
		writer.write('\n');

		while ((c = fis.read()) != -1) {
			writer.write(c);
		}

		writer.close();
		fis.close();

	}

	// vérifie caractère par caractère que la clef du doc est celle qu'on a
	// introduit
	// fonction inutilisée
	public static void VerifieHmac(String comHmac, String fileOut)
			throws IOException {

		File fichier = new File(fileOut);
		FileInputStream fis = new FileInputStream(fichier);

		int c;
		while ((c = fis.read()) != '\n') {
		}

		int cpt = 0;
		boolean juste = true;

		while ((c = fis.read()) != '\n') {
			if (Integer.valueOf(comHmac.toCharArray()[cpt]) % 256 != c) {
				System.out.println("Erreur, la clef est mauvaise, tricheur!");
				juste = false;
				break;
			}
			cpt++;
		}

		if (juste)
			System.out.println("La clef est juste! SUPER!! TRAUCOOL!!");

		fis.close();

	}

	// lis le document à l'exception de la deuxieme ligne, calcule le Hmac puis
	// le compare à celui trouvé dans le document
	public static void VerifieHmac(byte[] cle, String fileOut)
			throws IOException, NoSuchAlgorithmException {

		byte[] buffer, resume, hmac;
		MessageDigest fonction_de_hachage;
		fonction_de_hachage = MessageDigest.getInstance("SHA1");

		File fichier = new File(fileOut);
		FileInputStream fis = new FileInputStream(fichier);
		buffer = new byte[8192];
		hmac = new byte[8192];
		int nbOctetsLus = 0;

		fonction_de_hachage.update(cle, 0, (int) cle.length);

		int c;
		int cpt = 0;

		// lis la 1ere ligne
		while ((c = fis.read()) != '\n') {
			buffer[cpt++] = (byte) c;
		}
		buffer[cpt++] = (byte) c;

		// lis la 2eme ligne
		int i = 0;
		while ((c = fis.read()) != '\n') {
			hmac[i++] = (byte) c;
		}

		// lis le reste
		try {
			nbOctetsLus = fis.read(buffer, cpt, buffer.length - cpt);
		} catch (IOException e) {
			e.printStackTrace();
		}

		while (nbOctetsLus != -1) {
			fonction_de_hachage.update(buffer, 0, nbOctetsLus + cpt);
			cpt = 0;
			try {
				nbOctetsLus = fis.read(buffer);
			} catch (IOException e) {
				e.printStackTrace();
			}
		}

		try {
			fis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		resume = fonction_de_hachage.digest();

		String comHmac = "%UDC HMAC Ox" + toHex(resume);

		System.out.println("HMAC calculé sur le doc: " + comHmac);

		compareHmac(hmac, comHmac);

		fis.close();

	}

	// compare la clef calculée avec le secret et le doc à la clef se trouvant
	// dans le doc
	private static void compareHmac(byte[] hmac, String comHmac) {

		boolean juste = true;

		for (int i = 0; i < comHmac.length(); i++) {
			if (Integer.valueOf(comHmac.toCharArray()[i]) % 256 != hmac[i]) {
				System.out
						.println("Erreur, la clef trouvée dans le document ne correspond pas à celle calculée grace au document, tricheur!");
				juste = false;
				break;
			}
		}
		if (juste)
			System.out.println("La clef est juste! SUPER!! TRAUCOOL!!");

	}

	public static String toHex(byte[] donnees) {
		return javax.xml.bind.DatatypeConverter.printHexBinary(donnees);
	}

	public static void main(String[] args) throws IOException {

		String mdp = "25D83080C913EC17C5BC07B9D5112CB2A99FA705";
		byte[] hmac = null;
		String fileOut = "NotesOut.pdf";
		String fileIn = "Notes.pdf";

		try {
			try {
				hmac = CalculeHmac(mdp.getBytes(), fileIn);
			} catch (FileNotFoundException e) {
				e.printStackTrace();
			}
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		String comHmac = "%UDC HMAC Ox" + toHex(hmac);

		System.out.println("HMAC doc origine: " + comHmac);

		System.out.println("\nDocument Notes.pdf");
		InsereHmac(comHmac, fileIn, fileOut);
		try {
			VerifieHmac(mdp.getBytes(), fileOut);
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

		System.out
				.println("\nDocument frauduleux avec le HMAC de Notes.pdf recopié à la main");
		InsereHmac(comHmac, "faux.pdf", "fauxOut.pdf");
		try {
			VerifieHmac(mdp.getBytes(), "fauxOut.pdf");
		} catch (NoSuchAlgorithmException e) {
			e.printStackTrace();
		}

	}
}
