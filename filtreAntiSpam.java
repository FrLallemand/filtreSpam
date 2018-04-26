
import java.io.BufferedReader;
import java.io.FileInputStream;
import java.io.InputStream;
import java.io.InputStreamReader;


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
				if(line.length > 3){
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
	*/
	public void lire_message(){

	}

	public static void main(String[] args) {
		filtreAntiSpam fas = new filtreAntiSpam();
	}
}
