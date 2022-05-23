package gui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowEvent;
import javax.swing.JFrame;
import javax.swing.JButton;

public class Main {

	public static void main(String[] args) {
		JFrame frame = new JFrame();
		frame.setSize(100, 100);
		frame.setLocationRelativeTo(null);
		JButton close = new JButton("close");
        close.addActionListener(new ActionListener() {
        	public void actionPerformed(ActionEvent e) {
        		frame.dispatchEvent(new WindowEvent(frame, WindowEvent.WINDOW_CLOSING));
        	}
        });
        frame.add(close);
        frame.setVisible(true);
	}

}
