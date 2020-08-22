package campoMinato;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.Image;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

import java.util.ArrayList;
import java.util.Random;

/**
 * @author Marco De Meco
 * @version (ultima modifica) 06/07/2018
 */
public class Main {
	
	private static int caselleScoperte;
	private static int righe;
	private static int colonne;
	private static int mineTotali;
	private static int[][] campo;
	
	private static Casella[][] caselle;
	
	private static JPanel root;
	private static JPanel griglia;
	private static JPanel stato;
	private static JPanel pulsante;
	private static JFrame fin;
	
	private static URL sorrisoURL;
	private static URL vittoriaURL;
	private static URL sconfittaURL;
	
	private static ImageIcon sorriso;
	private static ImageIcon occhiali;
	private static ImageIcon triste;

	private static Timer t;
	private static JButton smile;
	 
	public static void repaint(){
		griglia.repaint();
		stato.repaint();
	}
	
	public static void costruisciCampo() {
		//La funzione può essere chiamata più volte quindi assicuriamoci di ripulire l'interfaccia grafica
		griglia.removeAll();
		root.remove(griglia);
		
		Random rand = new Random();
		int random;
		for(int i=0; i<campo.length; i++){
			for(int j=0; j<campo[0].length; j++){
				campo[i][j]=0;
			}
		}
		
		ArrayList<Integer> mine = new ArrayList<Integer>(mineTotali);
		random = rand.nextInt(righe*colonne-1);
		for(int i=0; i<mineTotali; i++){
			while(mine.contains(random)){
				random = rand.nextInt(righe*colonne);
			}
			mine.add(random);
		}
		
		int r;
		int c;
		for(int i:mine){
			r=(i/colonne);
			c=(i%colonne);
			campo[r][c]=10;
		}
		
		for(int riga=0; riga<righe; riga++){
			for(int colonna=0; colonna<colonne; colonna++){
				
				if(campo[riga][colonna]==10){
					
					for(int i=-1; i<2; i++) { 
						for(int j=-1; j<2; j++) {
							if(j==0 && i==0) continue;	
							else if(riga+i < righe && riga+i>=0 && colonna+j < colonne && colonna+j>=0 && campo[riga+i][colonna+j]!=10) campo[riga+i][colonna+j]++;
						}
					}
				}
			}
		}
		int valore;
		
		boolean bandiera=false;
		
		for(int i=0; i<righe; i++){
			for(int j=0; j<colonne; j++){
				valore = campo[i][j];
				
				/*
				 * Nella remota possibilità in cui l'utente metta delle bandiere e al suo primo click prenda una bomba,
				 * il campo verrà ricostruito lasciando invariate le posizioni delle bandiere
				 * 
				 * Assicuriamoci di catturare l'eccezione in caso le caselle non siano ancora state create
				 */
				try{
					bandiera = caselle[i][j].isBandiera();
				}catch(NullPointerException e){}
				
				caselle[i][j] = new Casella(valore,i,j);
				caselle[i][j].setBandiera(bandiera);
				
				griglia.add(caselle[i][j]);
			}
		}
		root.add(griglia);
		fin.setContentPane(root);
	}
	
	public static int bandiereAdiacenti(int riga, int colonna){
		int cont = 0;
		
		for(int i=-1; i<2; i++) { 
			for(int j=-1; j<2; j++) {
				if(j==0 && i==0) continue;
				else if(riga+i < righe && riga+i>=0 && colonna+j < colonne && colonna+j>=0) {
					if(caselle[riga+i][colonna+j].isBandiera()) cont++;
				}
			}
		}
		return cont;
	}
	
	public static void scopriAdiacenti(int riga, int colonna){
		for(int i=-1; i<2; i++) { 
			for(int j=-1; j<2; j++) {
				if(riga+i < righe && riga+i>=0 && colonna+j < colonne && colonna+j>=0) {
					/*
					 * questo if mi assicura che, una volta che, per qualsiasi motivo,
					 * la partita sia finita, l'esecutore esca da questa funzione e smetta di selezionare
					 * caselle evitando quindi di trovarsi ad iniziare una partita con caselle già scoperte
					 * da questa funzione
					 */
					if(Casella.isPrimoClick()) return;
					
					caselle[riga+i][colonna+j].seleziona();
				}
			}
		}
	}
	
	public static void scopriTutte(){
		for(Casella[] i:caselle){
			for(Casella j:i){
				j.seleziona();
			}
		}
		repaint();
		t.interrompiTimer();
		smile.setIcon(triste);
		
		Casella.setOsservazione(true);
		
		if(JOptionPane.showConfirmDialog(null, "Hai perso!\nVuoi iniziare un'altra partita?", "Fine partita", JOptionPane.YES_NO_OPTION)==0) rigioca();
	}
	
	public static void primoClick(int i, int j){
		while(campo[i][j]!=0){
			costruisciCampo();
		}
		caselle[i][j].seleziona();
		t.iniziaTimer();
	}
	
	public static void controllaVittoria(){
		caselleScoperte++;
		
		if(caselleScoperte==(righe*colonne-mineTotali)){
			t.interrompiTimer();
			smile.setIcon(occhiali);
			
			//Evitiamo che nel momento in cui si scelga di osservare la partita, l'utente continui a scoprire caselle
			Casella.setOsservazione(true);
			
			if(JOptionPane.showConfirmDialog(null, "Hai vinto! Hai impiegato "+t.getTempo()+" secondi\nVuoi iniziare un'altra partita?", "Fine partita", JOptionPane.YES_NO_OPTION)==0)
				rigioca();
		}
	}
	
	public static void rigioca() {
		/*
		 * questo metodo può essere chiamato da Smile anche se l'utente non ha ancora concluso la partita
		 * assicuriamoci, dunque, di fermare il thread che conta il tempo
		 */
		t.interrompiTimer();
		t.reset();
		
		smile.setIcon(sorriso);
		
		Casella.setContBandiere(0);
		Casella.setPrimoClick(true);
		Casella.setGameOver(false);
		Casella.setOsservazione(false);
		
		caselleScoperte = 0;
		
		//Dato che la funzione costruisciCampo memorizza le bandiere, assicuriamoci di resettare il tutto
		for(Casella[] i : caselle){
			for(Casella j : i){
				j.setBandiera(false);
			}
		}
		costruisciCampo();
	}
	
	public static void main(String[] args) {
		caselleScoperte=0;
		righe=9;
		colonne=9;
		mineTotali=10;
		campo = new int[righe][colonne];
		
		caselle = new Casella[righe][colonne];
		
		root = new JPanel(new BorderLayout());
		griglia = new JPanel(new GridLayout(righe, colonne));
		stato = new JPanel(new GridLayout(1, 3));
		pulsante = new JPanel(new FlowLayout(10, 34, 10));
		fin = new JFrame("Campo Minato");
		
		sorrisoURL = Main.class.getResource("/Smile.png");
		vittoriaURL = Main.class.getResource("/Vittoria.png");
		sconfittaURL = Main.class.getResource("/Sconfitta.png");
		
		sorriso = new ImageIcon(new ImageIcon(sorrisoURL).getImage().getScaledInstance(30, 30, Image.SCALE_DEFAULT));
		occhiali = new ImageIcon(new ImageIcon(vittoriaURL).getImage().getScaledInstance(30, 30, Image.SCALE_DEFAULT));
		triste = new ImageIcon(new ImageIcon(sconfittaURL).getImage().getScaledInstance(30, 30, Image.SCALE_DEFAULT));

		t = new Timer();
		t.setBackground(Color.lightGray);
		smile = new JButton();
		
		@SuppressWarnings("serial")
		JPanel bandierine = new JPanel() {
			@Override
			protected void paintComponent(Graphics g) {
				super.paintComponent(g);
				g.setFont(new Font("Dialog", Font.PLAIN, 20));
				g.setColor(Color.black);
				g.fillRect(22, 8, 65, 34);
				g.setColor(Color.red);
				g.drawString(Integer.toString(10-Casella.getContBandiere()), 55-(Integer.toString(10-Casella.getContBandiere()).length()*5), 33);
			}
		};
		bandierine.setBackground(Color.lightGray);
		
		costruisciCampo();
	
		griglia.setBackground(Color.lightGray);
		griglia.setBorder(BorderFactory.createLineBorder(Color.gray));
		
		smile.setPreferredSize(new Dimension(30, 30));
		smile.setBackground(Color.GRAY);
		smile.setBorder(BorderFactory.createLineBorder(Color.gray));
		smile.setIcon(sorriso);
		
		smile.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent arg0) {
				rigioca();
			}
		});
		
		pulsante.setBackground(Color.lightGray);
		pulsante.add(smile);
		
		stato.add(bandierine);
		stato.add(pulsante);
		stato.add(t);
		
		stato.setBackground(Color.LIGHT_GRAY);
		stato.setBorder(BorderFactory.createLineBorder(Color.gray));
		stato.setPreferredSize(new Dimension(100, 50));
		
		root.setBorder(BorderFactory.createLineBorder(Color.gray));
		root.add(stato, BorderLayout.NORTH);
		
		fin.setSize(300, 350);
		fin.setResizable(false);
		fin.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		fin.setLocationRelativeTo(null);
		fin.setVisible(true);
		
		JOptionPane.showMessageDialog(null, "Benvenuto a Campo Minato\n\n"
				+ "Comandi:\n\n"
				+ "- Click sinistro per scoprire le caselle\n"
				+ "- Click destro per bandiera\n"
				+ "- Pulsante smile per ricominciare\n"
				+ "- Click centrale o doppio click per scoprire le caselle adiacenti\n"
				+ "  (Solo nel caso in cui siano posizionate abbastanza bandierine)\n\n"
				+ "Buona fortuna!!!");
	}
}
