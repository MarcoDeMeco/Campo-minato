package campoMinato;

import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;

import javax.swing.JPanel;

@SuppressWarnings("serial")
public class Timer extends JPanel implements Runnable{
	
	private int tempo; 
	private Thread thread;
	
	//Creare un nuovo thread è indispensabile per contare il tempo 
	public void run() {
		while(true) {
			try {
				Thread.sleep(1000);
				tempo++;
				this.repaint();
			}catch(InterruptedException e) {
				break;
			}
		}
	}
	
	protected void paintComponent(Graphics g) {
		super.paintComponent(g);	
		g.setFont(new Font("Dialog", Font.PLAIN, 20));
		g.setColor(Color.black);
		g.fillRect(10, 8, 65, 34);
		g.setColor(Color.red);
		g.drawString(Integer.toString(tempo), 45-(Integer.toString(tempo).length()*6), 33);
	}
	
	//creo un nuovo thread (con il run di questa classe) ogni qualvolta che devo iniziare a contare il tempo...
	public void iniziaTimer(){
		thread = new Thread(this);
		thread.start();
	}
	
	//...e lo interrompo ogni volta che lo voglio fermare (con questo metodo non c'è modo per far ripartire il thread se non creandone uno nuovo)
	public void interrompiTimer() {
		thread.interrupt();
	}
	
	public int getTempo(){
		return tempo;
	}
	
	/*
	 * Dal momento in cui interrompo il thread, la variabile tempo ancora può servirmi
	 * (infatti nel momento in cui l'utente guarda una partita finita, sul pannello grafico
	 * verrà mostrato il tempo del vecchio thread), perciò faccio una funzione a parte
	 */
	public void reset(){
		tempo=0;
	}
}
