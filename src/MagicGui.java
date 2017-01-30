import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.EventQueue;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSlider;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import javax.swing.UIManager;
import javax.swing.border.EmptyBorder;


public class MagicGui extends JFrame {
	private JTextField txtItemName;
	private JTextField txtNpcLevel;
	private JPanel contentPane;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					MagicGui frame = new MagicGui();
					frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the frame.
	 */
	public MagicGui() {
		setTitle("Alch Splasher");
		setAlwaysOnTop(true);
		setResizable(false);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 294, 379);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		setContentPane(contentPane);
		contentPane.setLayout(null);
		
		JLabel lblName = new JLabel("Splash + Alcher");
		lblName.setForeground(Color.BLACK);
		lblName.setBackground(UIManager.getColor("Button.background"));
		lblName.setFont(new Font("Tahoma", Font.PLAIN, 27));
		lblName.setHorizontalAlignment(SwingConstants.CENTER);
		lblName.setBounds(10, 11, 258, 52);
		contentPane.add(lblName);
		
		JRadioButton rdbtnCurse = new JRadioButton("Confuse / Weaken / Curse");
		rdbtnCurse.setSelected(true);
		rdbtnCurse.setBounds(10, 70, 258, 23);
		contentPane.add(rdbtnCurse);
		
		JRadioButton rdbtnHighAlch = new JRadioButton("High Alch");
		rdbtnHighAlch.setBounds(10, 128, 109, 23);
		contentPane.add(rdbtnHighAlch);
		
		JLabel lblItemName = new JLabel("Item Name");
		lblItemName.setBounds(10, 158, 65, 14);
		contentPane.add(lblItemName);
		
		txtItemName = new JTextField();
		txtItemName.setBounds(91, 155, 177, 20);
		contentPane.add(txtItemName);
		txtItemName.setColumns(10);
		
		JLabel lblMaxTargetLvl = new JLabel("Max Target Lvl");
		lblMaxTargetLvl.setBounds(10, 103, 162, 14);
		contentPane.add(lblMaxTargetLvl);
		
		txtNpcLevel = new JTextField();
		txtNpcLevel.setHorizontalAlignment(SwingConstants.CENTER);
		txtNpcLevel.setText("10");
		txtNpcLevel.setColumns(10);
		txtNpcLevel.setBounds(182, 100, 86, 20);
		contentPane.add(txtNpcLevel);
		
		JLabel lblAntibanRate = new JLabel("Antiban Rate");
		lblAntibanRate.setHorizontalAlignment(SwingConstants.CENTER);
		lblAntibanRate.setBounds(10, 228, 258, 14);
		contentPane.add(lblAntibanRate);
		
		JSlider sldrAntibanRate = new JSlider();
		sldrAntibanRate.setValue(10);
		sldrAntibanRate.setMinorTickSpacing(1);
		sldrAntibanRate.setMajorTickSpacing(5);
		sldrAntibanRate.setMaximum(20);
		sldrAntibanRate.setSnapToTicks(true);
		sldrAntibanRate.setPaintTicks(true);
		sldrAntibanRate.setBounds(10, 253, 258, 23);
		contentPane.add(sldrAntibanRate);
		
		JButton btnNewButton = new JButton("Start");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				
				dispose();
			}
		});
		btnNewButton.setBounds(10, 287, 258, 45);
		contentPane.add(btnNewButton);
	}

}
