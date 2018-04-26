
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.List;

public class filtreAntiSpam {

	private List<String> dictionnaire;

	public filtreAntiSpam(){
		this.dictionnaire = new ArrayList<>();
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

	public static void main(String[] args) {
		filtreAntiSpam fas = new filtreAntiSpam();
	}
}
