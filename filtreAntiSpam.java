
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class filtreAntiSpam {

	private List<String> dictionnaire;
	private int nbSpam, nbHam;
	private int[] bSpam, bHam;

	private static final String spamDir = "baseapp/spam";
	private static final String hamDir = "baseapp/ham";

	public filtreAntiSpam(int spam, int ham){
		this.nbHam = ham;
		this.nbSpam = spam;
		this.dictionnaire = new ArrayList<>();
		this.charger_dictionnaire("dictionnaire1000en.txt");
		this.bSpam = this.apprentissage(spamDir, this.nbSpam);
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

	public int[] apprentissage(String dir, int nb){
		int words[] = new int[this.dictionnaire.size()];
		File folder = new File(dir);
		File[] files = folder.listFiles();
		for(int i=0; i<nb; i++){
			boolean message[] = this.lire_message(dir + "/" + files[i].getName());
			for(int j=0; j<message.length; j++){
				if(message[0]){
					words[j]++;
				}
			}
		}
		return words;
	}

	public static void main(String[] args) {
		int nbSpam = 200;
		int nbHam = 200;
		filtreAntiSpam fas = new filtreAntiSpam(nbSpam, nbHam);

	}
}
