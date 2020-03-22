package net.gizm0.twinkly;
import java.awt.BorderLayout;
import java.awt.EventQueue;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;
import java.awt.Font;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JTextField;
import javax.swing.JProgressBar;
import javax.swing.JLabel;
import javax.swing.JOptionPane;

import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.LineBorder;
import java.awt.Color;

public class GUI extends JFrame {

	private static final long serialVersionUID = 7790683854583707561L;
	
	private JPanel contentPane;
	private JTextField ipField;

	private TwinklyController twinkly;
	
	/**
	 * Launch the application.
	 * @param args the command line arguments. shouldn't be anything
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					GUI frame = new GUI();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
		try {
			UIManager.setLookAndFeel(UIManager.getSystemLookAndFeelClassName());
		} catch (Exception e) {
			try {
				UIManager.setLookAndFeel(UIManager.getCrossPlatformLookAndFeelClassName());
			} catch (Exception e1) {
				// Oh well. Better luck next time
			}
		}
	}

	/**
	 * Create the frame.
	 */
	public GUI() {
		setResizable(false);
		setTitle("Twinkly Lights Controller");
		setFont(new Font("Segoe UI", Font.PLAIN, 12));
		setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		setBounds(100, 100, 450, 300);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(new BorderLayout(0, 0));
		setContentPane(contentPane);
		
		JPanel panel = new JPanel();
		contentPane.add(panel, BorderLayout.NORTH);
		
		JButton btnScan = new JButton("Scan");
		btnScan.setEnabled(false);
		panel.add(btnScan);
		
		ipField = new JTextField();
		ipField.setText("192.168.1.185");
		ipField.setToolTipText("IP Address");
		panel.add(ipField);
		ipField.setColumns(15);
		
		JProgressBar progressBar = new JProgressBar();
		contentPane.add(progressBar, BorderLayout.SOUTH);
		
		JButton btnConnect = new JButton("Connect");
		btnConnect.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				try {
					progressBar.setIndeterminate(true);
					twinkly = new TwinklyController(ipField.getText());
					progressBar.setIndeterminate(false);
					JOptionPane.showMessageDialog(rootPane, "Connected!");
				} catch (Exception exception) {
					progressBar.setIndeterminate(false);
					JOptionPane.showMessageDialog(rootPane, "Could not connect to Twinkly at `" + ipField.getText() + "`. Did you type the IP address right?",
								"Connection Error",
								JOptionPane.ERROR_MESSAGE);
				}
			}
			
		});
		panel.add(btnConnect);
		
		JPanel panel_1 = new JPanel();
		panel_1.setBorder(new LineBorder(new Color(0, 0, 0)));
		contentPane.add(panel_1, BorderLayout.WEST);
		panel_1.setLayout(new GridLayout(4, 1, 0, 0));
		
		JLabel lblMode = new JLabel("Mode");
		lblMode.setHorizontalAlignment(SwingConstants.CENTER);
		panel_1.add(lblMode);
		
		JButton btnOff = new JButton("Off");
		btnOff.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (twinkly != null) {
					try {
						progressBar.setIndeterminate(true);
						twinkly.setMode(0);
						progressBar.setIndeterminate(false);
					} catch (Exception exception) {
						progressBar.setIndeterminate(false);
						JOptionPane.showMessageDialog(rootPane, "Something went wrong:\n" + exception.getLocalizedMessage(),
									"Error",
									JOptionPane.ERROR_MESSAGE);
					}
				}
			}
			
		});
		panel_1.add(btnOff);
		
		JButton btnDemo = new JButton("Demo");
		btnDemo.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (twinkly != null) {
					try {
						progressBar.setIndeterminate(true);
						twinkly.setMode(1);
						progressBar.setIndeterminate(false);
					} catch (Exception exception) {
						progressBar.setIndeterminate(false);
						JOptionPane.showMessageDialog(rootPane, "Something went wrong:\n" + exception.getLocalizedMessage(),
									"Error",
									JOptionPane.ERROR_MESSAGE);
					}
				}
			}
			
		});
		panel_1.add(btnDemo);
		
		JButton btnMovie = new JButton("Movie");
		btnMovie.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (twinkly != null) {
					try {
						progressBar.setIndeterminate(true);
						twinkly.setMode(2);
						progressBar.setIndeterminate(false);
					} catch (Exception exception) {
						progressBar.setIndeterminate(false);
						JOptionPane.showMessageDialog(rootPane, "Something went wrong:\n" + exception.getLocalizedMessage(),
									"Error",
									JOptionPane.ERROR_MESSAGE);
					}
				}
			}
			
		});
		panel_1.add(btnMovie);
		
		JButton btnUpload = new JButton("Upload Image");
		btnUpload.addActionListener(new ActionListener() {

			@Override
			public void actionPerformed(ActionEvent e) {
				if (twinkly != null) {
					JFileChooser fc = new JFileChooser();
					if (fc.showOpenDialog(rootPane) == JFileChooser.APPROVE_OPTION) {
						try {
							progressBar.setIndeterminate(true);
							twinkly.uploadMovie(fc.getSelectedFile(), 20);
							progressBar.setIndeterminate(false);
						} catch (Exception e1) {
							progressBar.setIndeterminate(true);
							JOptionPane.showMessageDialog(rootPane, "Something went wrong:\n" + e1.getLocalizedMessage(),
									"Error",
									JOptionPane.ERROR_MESSAGE);
						}
					}
				}
			}
		});
		contentPane.add(btnUpload, BorderLayout.EAST);
		
		JPanel panel_2 = new JPanel();
		contentPane.add(panel_2, BorderLayout.CENTER);
		panel_2.setLayout(new BorderLayout(0, 0));
//		tree.setModel(new DefaultTreeModel(
//			new DefaultMutableTreeNode("Tree (192.168.1.185)") {
//				{
//					DefaultMutableTreeNode node_1;
//					DefaultMutableTreeNode node_2;
//					node_1 = new DefaultMutableTreeNode("Hardware");
//						node_2 = new DefaultMutableTreeNode("Product");
//							node_2.add(new DefaultMutableTreeNode("Product Version: 16"));
//							node_2.add(new DefaultMutableTreeNode("Product Code: TW225SEUP07"));
//							node_2.add(new DefaultMutableTreeNode("Product Name: Twinkly"));
//						node_1.add(node_2);
//						node_2 = new DefaultMutableTreeNode("LEDs");
//							node_2.add(new DefaultMutableTreeNode("LED Type: 6"));
//							node_2.add(new DefaultMutableTreeNode("Base LEDs Number: 225"));
//							node_2.add(new DefaultMutableTreeNode("LED Version: 1"));
//							node_2.add(new DefaultMutableTreeNode("LED Profile: RGB"));
//							node_2.add(new DefaultMutableTreeNode("Number of LEDs: 225"));
//							node_2.add(new DefaultMutableTreeNode("Max Supported LEDs: 255"));
//						node_1.add(node_2);
//						node_1.add(new DefaultMutableTreeNode("Hardware Version: 7"));
//						node_1.add(new DefaultMutableTreeNode("MAC Address: 84:F3:EB:07:23:96"));
//						node_1.add(new DefaultMutableTreeNode("Hardware ID: 00072396"));
//						node_1.add(new DefaultMutableTreeNode("RSSI: -67"));
//					getContentPane().add(node_1);
//					node_1 = new DefaultMutableTreeNode("Software");
//						node_2 = new DefaultMutableTreeNode("Storage:");
//							node_2.add(new DefaultMutableTreeNode("Movie Capacity: 719"));
//							node_2.add(new DefaultMutableTreeNode("Flash Size: 16"));
//						node_1.add(node_2);
//						node_1.add(new DefaultMutableTreeNode("UUID: B66C9EF3-E6F2-48A2-B27A-A07CF8D5F3E1"));
//						node_1.add(new DefaultMutableTreeNode("Frame Rate: 25"));
//						node_1.add(new DefaultMutableTreeNode("Uptime: 3925323"));
//						node_1.add(new DefaultMutableTreeNode("Device Name: Tree"));
//						node_1.add(new DefaultMutableTreeNode("Copyright: LEDWORKS 2017"));
//					getContentPane().add(node_1);
//				}
//			}
//		));
	}

}
