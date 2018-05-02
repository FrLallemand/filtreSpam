import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Scanner;
import java.io.Serializable;

public class filtreAntiSpam implements Serializable{
	private static final long serialVersionUID = 1L;

	public static final String ANSI_RESET = "\u001B[0m";
	public static final String ANSI_RED = "\u001B[31m";
	public static final String ANSI_GREEN = "\u001B[32m";

	private List<String> dictionnaire;
	private double nbSpam, nbHam;
	private double probaSpam, probaHam;

	private double[] bSpam, bHam;

	private static final String spamDir = "baseapp/spam";
	private static final String hamDir = "baseapp/ham";

	private static final String testSpamDir = "basetest/spam";
	private static final String testHamDir = "basetest/ham";

	private static final double epsilon = 1;

	public filtreAntiSpam(int spam, int ham){
		this.nbHam = (double)ham;
		this.nbSpam = (double)spam;
		this.dictionnaire = new ArrayList<>();
	}

	public void apprentissage(){
		System.out.printf("Apprentissage sur: %d spams et %d hams...\n", (long)this.nbSpam, (long)this.nbHam);
		this.bSpam = new double[this.dictionnaire.size()];
		this.bHam = new double[this.dictionnaire.size()];
		double[] apparitionSpam = this.apparitionSpamHam(spamDir, this.nbSpam);
		double[] apparitionHam = this.apparitionSpamHam(hamDir, this.nbHam);

		for(int i=0; i<this.dictionnaire.size(); i++){
			// calcule bjSpam
			this.bSpam[i] = (apparitionSpam[i] + epsilon) / (nbSpam + 2*epsilon);
			//System.out.println("On a calculé bSpam = " + this.bSpam[i] + " pour " + this.dictionnaire.get(i) + " car il apparaissait : " + apparitionSpam[i] + " fois");
			// calcule bjHam
			this.bHam[i] = (apparitionHam[i] + epsilon) / (nbHam + 2*epsilon);
		}

		// estimation des probabilités
		this.probaSpam = nbSpam / (nbSpam + nbHam);
		this.probaHam = 1 - this.probaSpam;

		// System.out.println("P( Y = SPAM ) = "+ this.probaSpam +" P( Y = HAM ) = "+ this.probaHam);
	}

	public void apprentissageV2(boolean[] mail, boolean spam){

		if(spam){
			this.nbSpam++;
			for(int i=0; i<this.dictionnaire.size(); i++){
				// calcule mjSpam
				// bjspam = (njspam + epsilon) / (mjspam + 2*epsilon )
				// => mjspam = bjspam * (mjspam + 2*epsilon ) - epsilon
				double njSpam = (this.bSpam[i] + (this.nbSpam + 2*epsilon))- epsilon;
				double x = 0;
				if(mail[i]){
					x = 1;
				}
				this.bSpam[i] = (njSpam + x + epsilon) / (this.nbSpam + 1 + 2*epsilon);
			}
		} else {
			this.nbHam++;
			for(int i=0; i<this.dictionnaire.size(); i++){
				double njHam = (this.bHam[i] + (this.nbHam + 2*epsilon))- epsilon;
				double x = 0;
				if(mail[i]){
					x = 1;
				}
				this.bHam[i] = (njHam + x + epsilon) / (this.nbHam + 1 + 2*epsilon);
			}
		}



		// estimation des probabilités
		this.probaSpam = nbSpam / (nbSpam + nbHam);
		this.probaHam = 1 - this.probaSpam;

		// System.out.println("P( Y = SPAM ) = "+ this.probaSpam +" P( Y = HAM ) = "+ this.probaHam);
	}

	public void printApprentissage(){
		for(int i = 0; i < this.dictionnaire.size(); i++){
			System.out.println(dictionnaire.get(i) + " =  SPAM : " + this.bSpam[i] + " / HAM : " + this.bHam[i]);
		}
	}

	public double[] apparitionSpamHam(String dir, double nb){
		double[] words = new double[this.dictionnaire.size()];
		File folder = new File(dir);
		File[] files = folder.listFiles();
		for(int i=0; i<nb; i++){
			boolean message[] = this.lire_message(dir + "/" + files[i].getName());
			for(int j=0; j<message.length; j++){
				if(message[j]){
					words[j]++;
				}
			}
		}
		return words;
	}


	/*
	  Fonction charger_dictionnaire : cette fonction doit pouvoir charger un dictionnaire (par
	  exemple dans un tableau de mots) à partir d’un fichier texte.
	*/
	public void charger_dictionnaire(String path){
		try{
			InputStream input = new FileInputStream(path);
			BufferedReader br=new BufferedReader(new InputStreamReader(input));

			String line;
			while((line = br.readLine()) != null){
				if(line.length() >= 3){
					this.dictionnaire.add(line);
				}
			}
			br.close();
		} catch(Exception e) {
			e.printStackTrace();
		}
	}

	/*
	  Fonction lire_message : cette fonction doit pouvoir lire un message (dans un fichier texte) et le
	  traduire en une représentation sous forme de vecteur binaire x à partir d’un dictionnaire.

	  Seule la présence des mots est prise en compte, par le nombre d'occurences.
	*/
	public boolean[] lire_message(String path){
		// false par défaut
		boolean[] vecteur = new boolean[this.dictionnaire.size()];

		try{
			InputStream input = new FileInputStream(path);
			BufferedReader br=new BufferedReader(new InputStreamReader(input));

			String line;
			while((line = br.readLine()) != null){
				String[] words = line.split("\\W+");
				for(String word : words){
					if(word.length() >= 3){
						word = word.toUpperCase();
						if(this.dictionnaire.contains(word)){
							vecteur[this.dictionnaire.indexOf(word)] = true;
						}
					}
				}
			}
			br.close();
		} catch(Exception e) {
			e.printStackTrace();
		}

		return vecteur;
	}

	public boolean isSpam(boolean[] mail){
		double pSpam = 0;
		double pHam = 0;

		// pitoyable tentative d'utiliser log
		for(int i=0; i<this.dictionnaire.size(); i++){
			if(mail[i]) { // il y a une occurence du mot i
				pSpam += Math.log(this.bSpam[i]);
				pHam += Math.log(this.bHam[i]);
			} else { // le mot i n'est pas dans le mail
				pSpam += Math.log(1 - this.bSpam[i]);
				pHam += Math.log(1 - this.bHam[i]);
			}
		}
		pSpam += this.probaSpam;
		pHam += this.probaHam;

		if(pSpam > pHam){
			return true;
		} else {
			return false;
		}
	}

	public void save(String destination){
		try{
			FileOutputStream file = new FileOutputStream(new File(destination));
			ObjectOutputStream oos = new ObjectOutputStream(file);
			oos.writeObject(this);
			oos.close();
		} catch (FileNotFoundException e) {
			System.out.println("File not found");
		} catch (IOException e) {
			System.out.println("Error initializing stream");
		}
	}


	public filtreAntiSpam load(String source){
		filtreAntiSpam fas = null;
		try{
			FileInputStream file = new FileInputStream(new File(source));
			ObjectInputStream ois = new ObjectInputStream(file);
			fas = (filtreAntiSpam) ois.readObject();
			ois.close();
		} catch (FileNotFoundException e) {
			System.out.println("File not found");
		} catch (IOException e) {
			System.out.println("Error initializing stream");
		} catch (ClassNotFoundException e) {
			e.printStackTrace();
		}
		return fas;
	}


	public static void main(String[] args) {
		String firstArg = args[0];
		if((args.length == 4) && (firstArg.equals("save"))){
			//sauvegarde du classifieur
			String destination = args[1];
			int nbSpam = Integer.parseInt(args[2]);
			int nbHam = Integer.parseInt(args[3]);
			filtreAntiSpam fas = new filtreAntiSpam(nbSpam, nbHam);

			fas.charger_dictionnaire("dictionnaire1000en.txt");
			fas.apprentissage();
			fas.save(destination);
			System.out.printf("Classifieur enregistré dans: %s.\n", destination);
		} else if((args.length == 4) && (firstArg.equals("filtre_ligne"))){
			//utilisation d'un classifieur sauvegardé, en ligne
			String source = args[1];
			String mail = args[2];
			String mailType = args[3];
			boolean spam = false;
			if(mailType.equals("SPAM")) {
				spam = true;
			} else if(mailType.equals("HAM")){
				spam = false;
			} else {
				System.out.printf("Must be SPAM or HAM\n");
				System.exit(-1);
			}
			filtreAntiSpam fas = new filtreAntiSpam(0, 0);
			fas = fas.load(source);

			fas.apprentissageV2(fas.lire_message(mail), spam);

			fas.save(source);

			if(spam) {
				System.out.printf("Modification du filtre %s par apprentissage sur le SPAM %s.\n", source, mail);
			} else{
						System.out.printf("Modification du filtre %s par apprentissage sur le HAM %s.\n", source, mail);
			}

		}else if (args.length == 3){
			//execution "classique"
			String testFolder = args[0];
			int nbSpamInTest = Integer.parseInt(args[1]);
			int nbHamInTest = Integer.parseInt(args[2]);

			int nbSpam;
			int nbHam;

			Scanner reader = new Scanner(System.in);

			System.out.print("Nombre de spams dans la base d'apprentissage : ");
			nbSpam = reader.nextInt();
			System.out.println("");
			System.out.print("Nombre de hams dans la base d'apprentissage : ");
			nbHam = reader.nextInt();
			System.out.println("");

			filtreAntiSpam fas = new filtreAntiSpam(nbSpam, nbHam);

			fas.charger_dictionnaire("dictionnaire1000en.txt");
			fas.apprentissage();

			System.out.println("=== TEST ===\n");

			double nbCorrectSpam = 0;
			double nbCorrectHam = 0;

			for(int i=0; i<nbSpamInTest; i++){
				boolean a = fas.isSpam(fas.lire_message(testFolder + "/spam/" + i + ".txt"));

				if(a){
					System.out.println(ANSI_GREEN + "Spam number " + i + " has been classified as a SPAM" + ANSI_RESET);
					nbCorrectSpam++;
				}
				else{
					System.out.println(ANSI_RED + "Spam number " + i + " has been classified as a HAM" + ANSI_RESET);
				}
			}

			for(int i=0; i<nbHamInTest; i++){
				boolean a = fas.isSpam(fas.lire_message(testFolder + "/ham/" + i + ".txt"));

				if(!a){
					System.out.println(ANSI_GREEN + "Ham number " + i + " has been classified as a HAM" + ANSI_RESET);
					nbCorrectHam++;
				}
				else{
					System.out.println(ANSI_RED + "HAM number " + i + " has been classified as a SPAM" + ANSI_RESET);
				}
			}

			double spamError = (nbSpamInTest - nbCorrectSpam) / nbSpamInTest;
			double hamError = (nbHamInTest - nbCorrectHam) / nbHamInTest;
			double globalError = (nbSpamInTest + nbHamInTest - nbCorrectSpam - nbCorrectHam) / (nbHamInTest + nbSpamInTest);

			System.out.println("Test error on " + nbSpamInTest + " spams : " + spamError + "%");
			System.out.println("Test error on " + nbHamInTest + " hams : " + hamError + "%");
			System.out.println("Global test error : " + globalError + "%");
		} else if((args.length == 2)){
			//utilisation d'un classifieur sauvegardé
			String source = args[0];
			String mail = args[1];

			filtreAntiSpam fas = new filtreAntiSpam(0, 0);
			fas = fas.load(source);

			boolean a = fas.isSpam(fas.lire_message(mail));
			if(a){
				System.out.printf("D’après ’%s’, le message ’%s’ est un SPAM !\n", source, mail);
			} else {
				System.out.printf("D’après ’%s’, le message ’%s’ est un HAM !\n", source, mail);
			}
		}else {
			System.out.println("usage : java filtreAntiSpam <test_folder> <number of spam in test folder> <number of ham in test folder>");
			System.out.println("usage : java filtreAntiSpam save <file where to store classifier > <number of spam> <number of ham>");
			System.out.println("usage : java filtreAntiSpam filtre_ligne <source file for the classifier > <message> <HAM or SPAM>");
			System.out.println("usage : java filtreAntiSpam <source file for the classifier > <message to test>");
			System.exit(-1);
		}

	}
}
