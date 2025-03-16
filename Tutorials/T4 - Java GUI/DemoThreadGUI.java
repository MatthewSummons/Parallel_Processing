import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class DemoThreadGUI {

	public static void main(String[] args) {
		SwingUtilities.invokeLater(() -> new DemoThreadGUI().go());
	}

	public void go() {
		JFrame frame = new JFrame("Oval Animation");
		frame.add(new MyPanel());
		frame.pack();
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.setVisible(true);
	}

	class MyPanel extends JPanel implements MouseListener {
		int x = 50, y = 50, r = 25;

		MyPanel() {
			setPreferredSize(new Dimension(100, 100));
			addMouseListener(this);
		}

		public void paintComponent(Graphics g) {
			super.paintComponent(g);
			g.fillOval(x - r, y - r, r * 2, r * 2);
		}

		public void mouseClicked(MouseEvent event) {
			new Animation(this, event.getX(), event.getY()).start();
		}

		public void mouseEntered(MouseEvent e) {}
		public void mouseExited(MouseEvent e) {}
		public void mousePressed(MouseEvent e) {}
		public void mouseReleased(MouseEvent e) {}
	}

	class Animation {
		private final Timer timer;
		private final MyPanel panel;
		private int steps = 0;
		private final int maxSteps = 20;
		private final int targetX, targetY;

		Animation(MyPanel panel, int tx, int ty) {
			this.panel = panel;
			targetX = tx;
			targetY = ty;
			timer = new Timer(30, e -> animate());
		}

		void start() {
			timer.start();
		}

		private void animate() {
			panel.x = (panel.x + targetX) / 2;
			panel.y = (panel.y + targetY) / 2;
			panel.repaint();
			if (++steps >= maxSteps) {
				timer.stop();
			}
		}
	}
}
