package campoMinato;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.BorderFactory;
import javax.swing.JPanel;

@SuppressWarnings("serial")
public class Casella extends JPanel implements MouseListener {
	
	private static Color scoperto;
	private static Color coperto;
	private static Color mina;
	private static Casella temp;
	private static int contBandiere;
	private static boolean premuto;
	private static boolean gameOver;
	private static boolean primoClick;
	private static boolean staOsservando;
	
	
	private int mineAdiacenti;
	private int riga;
	private int colonna;
	private Color num;
	private boolean selezionato;
	private boolean bandiera;
	
	static {
		primoClick = true;
		scoperto = Color.green;
		coperto = Color.gray;
		mina = Color.red;
	}
	
	{
		setSize(new Dimension(50, 50));
		setBackground(coperto);
		addMouseListener(this);
		setBorder(BorderFactory.createLineBorder(Color.black));
	}
	
	public Casella(int mineAdiacenti, int riga, int colonna){
		this.mineAdiacenti = mineAdiacenti;
		this.riga = riga;
		this.colonna = colonna;
		
		switch (mineAdiacenti) {
		case 1: num = Color.blue;
			break;
		case 2: num = Color.magenta;
			break;
		case 3: num = Color.red;
			break;
		case 4: num = Color.black;
			break;
		case 5: num = Color.darkGray;
			break;
		case 6: num = Color.orange;
			break;
		case 7: num = Color.white;
			break;
		case 8: num = Color.gray;
			break;
		}
	}
	
	@Override
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);
		
		g.setFont(new Font("Dialog", Font.BOLD, 16));
		if(bandiera && !gameOver){
			try{
				g.drawImage(ImageIO.read(getClass().getResource("/Bandiera.png")), 5, 5, 20, 20, null);
			}catch (IOException e) {}
		}
		
		if(selezionato && mineAdiacenti!=0){
			g.setColor(num);
			
			if(mineAdiacenti!=10){
				g.drawString(Integer.toString(mineAdiacenti), 11, 20);
				
				if(bandiera && gameOver){
					try{
						g.drawImage(ImageIO.read(getClass().getResource("/CroceRossa.png")), 0, -1, 30, 30, null);
					}catch (IOException e) {}
				}
			}else{
				try{
					g.drawImage(ImageIO.read(getClass().getResource("/Mina.png")), 6, 5, 20, 20, null);
				}catch (IOException e) {}
				
				if(bandiera && gameOver){
					try{
						g.drawImage(ImageIO.read(getClass().getResource("/CroceVerde.png")), 0, -1, 30, 30, null);
					}catch (IOException e) {}
				}
			}	
		}
	}

	public void seleziona(){
		if(!selezionato){
			
			//Nel caso in cui la funzione viene chiamata per scoprire tutte le caselle in game over
			if(bandiera && gameOver){
				selezionato=true;
				if(mineAdiacenti==10) setBackground(mina);
				else setBackground(scoperto);
				
			}else if(!bandiera){
				if(mineAdiacenti==10){
					setBackground(mina);
					selezionato = true;
					if(!gameOver){
						gameOver = true;
						Main.scopriTutte();
					}
				}else{
					setBackground(scoperto);
					selezionato = true;
					if(!gameOver){
						Main.repaint();
						Main.controllaVittoria();
						if(mineAdiacenti==0) Main.scopriAdiacenti(this.riga, this.colonna);
					}
				}
			}
		}
	}
		
	public boolean isSelezionato() {
		return selezionato;
	}
	
	public boolean isBandiera() {
		return bandiera;
	}

	public void setBandiera(boolean bandiera) {
		this.bandiera = bandiera;
	}
	
	public static void setContBandiere(int contBandiere) {
		Casella.contBandiere = contBandiere;
	}
	
	public static int getContBandiere() {
		return contBandiere;
	}

	public static void setGameOver(boolean gameOver) {
		Casella.gameOver = gameOver;
	}
	
	public static boolean isGameOver(){
		return gameOver;
	}

	public static void setPrimoClick(boolean primoClick) {
		Casella.primoClick = primoClick;
	}
	
	public static boolean isPrimoClick() {
		return primoClick;
	}
	
	public static void setOsservazione(boolean osservazione) {
		Casella.staOsservando = osservazione;
	}

	
	@Override
	public void mouseReleased(MouseEvent e) {
		if(!staOsservando && premuto) {
			premuto=false;
			if(e.getButton()==MouseEvent.BUTTON1){
				if(primoClick && !temp.bandiera){
					primoClick = false;
					Main.primoClick(temp.riga, temp.colonna);
				}else temp.seleziona();
			}
		}
	}
	  
	public void mousePressed(MouseEvent e) {
		/*
		 * Nel caso in cui l'utente prema il tasto sinistro del mouse su una casella scoperta
		 * e lo tenga premuto fino a portarlo in una casella coperta, questa non viene selezionata
		 */
		if(!temp.selezionato && !staOsservando){
			if(e.getButton()==MouseEvent.BUTTON3){
				if(bandiera){
					contBandiere--;
					temp.bandiera=false;
				}else{
					contBandiere++;
					temp.bandiera=true;
				}
				Main.repaint();
			}else if(e.getButton()==MouseEvent.BUTTON1){
				if(!bandiera) {
					setBackground(scoperto);
					premuto = true;
				}
			}
		}
	}
	
	public void mouseEntered(MouseEvent e) {
		/*
		* Utilizzando la casella temp la funzione non seleziona la casella su cui l'utente
		* ha premuto il tasto (come farebbe di default), ma seleziona l'ultima
		* su cui � stato il cursore prima che il tasto sia rilasciato
		*/
		temp = this;
		if(!selezionato){
			if(!premuto) setBackground(Color.lightGray);
			else if(!temp.bandiera) setBackground(scoperto);
		}
	}
	
	public void mouseExited(MouseEvent e) {
		if(!selezionato) setBackground(coperto);
	}
	
	public void mouseClicked(MouseEvent e) {
		if(!staOsservando) {
			if(e.getButton()==MouseEvent.BUTTON2){
				if(Main.bandiereAdiacenti(riga, colonna)>=mineAdiacenti) Main.scopriAdiacenti(riga, colonna); 
			}
			
			/*
			 * La funzione getClickCount mi consente di capire se il giocatore ha cliccato due volte
			 * consecutivamente entro un intervallo di tempo preso di default dal sistema
			 */
			if(e.getButton()==MouseEvent.BUTTON1 && e.getClickCount()>1) {
				if(Main.bandiereAdiacenti(riga, colonna)>=mineAdiacenti) {
					Main.scopriAdiacenti(riga, colonna);
				}
			}
		}
	}
}
