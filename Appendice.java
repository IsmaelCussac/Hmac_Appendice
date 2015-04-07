import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigInteger;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

public class Appendice {

	// Calcule la signature à partir du document
	public static BigInteger CalculeAppendice(BigInteger d, BigInteger n,
			String fileIn) throws NoSuchAlgorithmException,
			FileNotFoundException {

		byte[] buffer, resume;
		MessageDigest fonction_de_hachage;
		fonction_de_hachage = MessageDigest.getInstance("SHA1");

		File fichier = new File(fileIn);
		FileInputStream fis = new FileInputStream(fichier);
		buffer = new byte[8192];
		int nbOctetsLus = 0;

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

		// digere
		resume = fonction_de_hachage.digest();

		BigInteger M = new BigInteger(toHex(resume), 16);

		// déchiffre le résumé du document
		BigInteger a = M.modPow(d, n);

		try {
			fis.close();
		} catch (IOException e) {
			e.printStackTrace();
		}

		return a;
	}

	// insere le commentaire de la signature à la ligne 2
	public static void InsereAppendice(String comSign, String fileIn,
			String fileOut) throws IOException {

		FileInputStream fis = new FileInputStream(fileIn);

		FileOutputStream writer = new FileOutputStream(fileOut);

		int c;

		while ((c = fis.read()) != '\n') {
			writer.write(c);
		}
		writer.write('\n');

		writer.write(comSign.getBytes());
		writer.write('\n');

		while ((c = fis.read()) != -1) {
			writer.write(c);
		}

		writer.close();
		fis.close();

	}

	// vérifie que la signature chiffrée du doc soit bien égale au résumé du doc
	public static void VerifieAppendice(BigInteger e, BigInteger n,
			String fileOut) throws IOException, NoSuchAlgorithmException {

		BigInteger a = chiffreAppendice(e, n, fileOut);

		BigInteger M = calculeSha1Doc(fileOut);

		if (a.byteValue() != M.byteValue())
			System.out
					.println("Erreur, la signature est chiffrée lue dans le doc est différent du résumé du document, tricheur!");
		else
			System.out.println("La signature est juste! SUPER!! TRAUCOOL!!");
	}

	// lis le document et construit son résumé
	private static BigInteger calculeSha1Doc(String fileOut)
			throws NoSuchAlgorithmException, IOException {
		byte[] buffer, resume;
		MessageDigest fonction_de_hachage;
		fonction_de_hachage = MessageDigest.getInstance("SHA1");

		File fichier = new File(fileOut);
		FileInputStream fis = new FileInputStream(fichier);
		buffer = new byte[8192];

		int nbOctetsLus = 0;

		int c;
		int cpt = 0;

		// lis la 1ere ligne
		while ((c = fis.read()) != '\n') {
			buffer[cpt++] = (byte) c;
		}
		buffer[cpt++] = (byte) c;

		// lis la 2eme ligne et ne la prend pas en compte
		while ((c = fis.read()) != '\n') {
		}

		// lis le reste
		try {
			nbOctetsLus = fis.read(buffer, cpt, buffer.length - cpt);
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		while (nbOctetsLus != -1) {
			fonction_de_hachage.update(buffer, 0, nbOctetsLus + cpt);
			cpt = 0;
			try {
				nbOctetsLus = fis.read(buffer);
			} catch (IOException e1) {
				e1.printStackTrace();
			}
		}

		try {
			fis.close();
		} catch (IOException e1) {
			e1.printStackTrace();
		}

		// digere
		resume = fonction_de_hachage.digest();

		System.out.println("Résumé du ficher: " + toHex(resume));

		fis.close();

		// retourne le résumé sous forme de BigInteger
		return new BigInteger(resume);
	}

	// chiffre l'appendice lu dans le document et le renvoie
	private static BigInteger chiffreAppendice(BigInteger e, BigInteger n,
			String fileOut) throws IOException {
		BufferedReader br = new BufferedReader(new InputStreamReader(
				new FileInputStream(fileOut)));
		br.readLine();

		String com = br.readLine();
		String[] res = com.split("x");

		BigInteger M = new BigInteger(res[1], 16);

		BigInteger a = M.modPow(e, n);
		System.out.println("Appendice chiffré: " + toHex(a.toByteArray()));

		br.close();
		return a;

	}

	public static String toHex(byte[] donnees) {
		return javax.xml.bind.DatatypeConverter.printHexBinary(donnees);
	}

	public static void main(String[] args) throws IOException {

		BigInteger n = new BigInteger(
				"22D352B7347CA7E3814FCBEC5781F808D9574CF63229E001F94C8EB301EED65AF2DF6C12954FBEBA7187F57934C8BCFD15A8F9DCC6706324EF1A2DA9C741B2B581836CD45B2B6FA90F0DAE95466E637CD034E6E7638D606F4793AE9B6D872B104306AA2F92F5530842FFAD15117549D991C4FB5F5AB17E8E2DF149343D49249C1BDA6167",
				16);
		BigInteger e = new BigInteger(
				"26A3342507BA10D94C243C9FCC4EF13EE32D6AF64F181ED5CB761F6678C91196926A8EF4FC91D61A2EF1929B1238A95724C9EAE626F1201BA08A3FE416ABA166F91A55A178346D988AC631A6498E99C7EE68D6A69CE34E948E9ACF1EAD4E61BCAC2E19ED40875C76DB35CDCD56331CC2F148CEB303CD04F2F120DE639BB39598FEC82D",
				16);
		BigInteger d = new BigInteger(
				"25D83080C913EC17C5BC07B9D5112CB2A99FA705", 16);

		String fileOut = "NotesOut.pdf";
		String fileIn = "Notes.pdf";
		BigInteger signature = null;
		try {
			try {
				signature = CalculeAppendice(d, n, fileIn);
			} catch (FileNotFoundException e1) {
				e1.printStackTrace();
			}
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		}

		String comSign = "%UDC SIGNATURE Ox" + toHex(signature.toByteArray());

		System.out.println("SIGNATURE: " + comSign + "\n");

		System.out.println("Document Notes.pdf");
		InsereAppendice(comSign, fileIn, fileOut);
		try {
			VerifieAppendice(e, n, fileOut);
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		}

		System.out
				.println("\nDocument frauduleux avec la signature de Notes.pdf recopié à la main");
		InsereAppendice(comSign, "faux.pdf", "fauxOut.pdf");
		try {
			VerifieAppendice(e, n, "fauxOut.pdf");
		} catch (NoSuchAlgorithmException e1) {
			e1.printStackTrace();
		}

	}
}
