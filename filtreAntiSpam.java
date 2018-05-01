
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class filtreAntiSpam {

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
		System.out.println("Apprentissage");

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

		System.out.println("P( Y = SPAM ) = "+ this.probaSpam +" P( Y = HAM ) = "+ this.probaHam);
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

		//pSpam = Math.exp(pHam - pSpam);
		//	pHam = Math.exp(pSpam - pHam);
		System.out.println("P( Y=SPAM | X=x ) =" + pSpam );
 		System.out.println("P( Y=HAM | X=x ) =" + pHam);

		if(pSpam > pHam){
			return true;
		} else {
			return false;
		}

		/*
		double proba = 0;
		double probap = 0;
		double partie1p, partie2p, partie1, partie2;
		double biSpam;
		double biHam;
		for(int i=0; i<this.dictionnaire.size(); i++){
			double b = 0.0;
			if(mail[i]){
				b = 1.0;
			}
			biSpam = bSpam[i];
			biHam = bHam[i];

			//System.out.println(spam.dictionnaire[i]+" "+biSpam);
			partie1 = (double)(Math.pow((double)(biSpam),b));
			partie2 = (double)(Math.pow((double)(1-(double)(biSpam)),(double)(1-b)));
			proba = (double)((double)(proba)+Math.log((double)((double)(partie1)*(double)(partie2))));

			partie1p = (double)(Math.pow((double)(biHam),b));
			partie2p = (double)(Math.pow((double)(1-(double)(biHam)),(double)(1-b)));
			probap = (double)((double)(proba)+Math.log((double)((double)(partie1p)*(double)(partie2p))));
		}

		System.out.println("P( Y=SPAM | X=x ) =" + proba );
 		System.out.println("P( Y=HAM | X=x ) =" + probap);

		if(proba > probap){
			return true;
		} else {
			return false;
		}
		*/
	}


	public static void main(String[] args) {
		int nbSpam = 200;
		int nbHam = 200;
		filtreAntiSpam fas = new filtreAntiSpam(nbSpam, nbHam);

		fas.charger_dictionnaire("dictionnaire1000en.txt");
		fas.apprentissage();
		//fas.printApprentissage();

		for(int i=0; i<20; i++){
			boolean a = fas.isSpam(fas.lire_message("basetest/spam/"+i+".txt"));

			System.out.println(a);
		}
	}
}
