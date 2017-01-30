import java.awt.EventQueue;

import javax.swing.JFrame;

import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JLabel;

import javax.swing.JSpinner;
import javax.swing.SpinnerNumberModel;
import javax.swing.JRadioButton;
import javax.swing.JTextField;
import javax.swing.SwingConstants;
import java.awt.Font;
import javax.swing.JSeparator;
import javax.swing.JFormattedTextField;
import javax.swing.JTextPane;


public class NmzGui {

	private JFrame frame;
	private JTextField textField;
	private JTextField textField_1;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					NmzGui window = new NmzGui();
					window.frame.setVisible(true);
				} catch (Exception e) {
					e.printStackTrace();
				}
			}
		});
	}

	/**
	 * Create the application.
	 */
	public NmzGui() {
		initialize();
	}

	/**
	 * Initialize the contents of the frame.
	 */
	private void initialize() {
		frame = new JFrame();
		frame.setBounds(100, 100, 574, 460);
		frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
		frame.getContentPane().setLayout(null);
		
		JPanel panel = new JPanel();
		panel.setBounds(0, 0, 558, 422);
		frame.getContentPane().add(panel);
		panel.setLayout(null);
		
		JButton btnNewButton = new JButton("Start!");
		btnNewButton.setBounds(10, 375, 538, 36);
		panel.add(btnNewButton);
		
		JLabel lblNewLabel = new JLabel("Spec Percentage");
		lblNewLabel.setBounds(10, 83, 146, 14);
		panel.add(lblNewLabel);
		
		JLabel lblMaxHp = new JLabel("Max Hp");
		lblMaxHp.setBounds(10, 108, 146, 14);
		panel.add(lblMaxHp);
		

		
		JSpinner spinner = new JSpinner();
		spinner.setModel(new SpinnerNumberModel(0, 0, 28, 1));
		spinner.setBounds(127, 241, 29, 20);
		panel.add(spinner);
		
		JSpinner spinner_1 = new JSpinner();
		spinner_1.setModel(new SpinnerNumberModel(0, 0, 28, 1));
		spinner_1.setBounds(127, 272, 29, 20);
		panel.add(spinner_1);
		
		JSpinner spinner_2 = new JSpinner();
		spinner_2.setModel(new SpinnerNumberModel(0, 0, 28, 1));
		spinner_2.setBounds(127, 303, 29, 20);
		panel.add(spinner_2);
		
		JLabel lblPrayerPotion = new JLabel("Prayer Potion");
		lblPrayerPotion.setBounds(10, 244, 107, 14);
		panel.add(lblPrayerPotion);
		
		JLabel lblOverloadPotion = new JLabel("Overload Potion");
		lblOverloadPotion.setBounds(10, 275, 107, 14);
		panel.add(lblOverloadPotion);
		
		JLabel lblAbsorptionPotion = new JLabel("Absorption Potion");
		lblAbsorptionPotion.setBounds(10, 306, 107, 14);
		panel.add(lblAbsorptionPotion);
		
		JRadioButton rdbtnNewRadioButton = new JRadioButton("Prayer Method");
		rdbtnNewRadioButton.setHorizontalAlignment(SwingConstants.LEFT);
		rdbtnNewRadioButton.setBounds(10, 188, 146, 23);
		panel.add(rdbtnNewRadioButton);
		
		JRadioButton rdbtnAbsorptionMethod = new JRadioButton("Absorption Method");
		rdbtnAbsorptionMethod.setHorizontalAlignment(SwingConstants.LEFT);
		rdbtnAbsorptionMethod.setBounds(10, 214, 146, 23);
		panel.add(rdbtnAbsorptionMethod);
		
		textField = new JTextField();
		textField.setBounds(127, 80, 86, 20);
		panel.add(textField);
		textField.setColumns(10);
		
		textField_1 = new JTextField();
		textField_1.setColumns(10);
		textField_1.setBounds(127, 105, 86, 20);
		panel.add(textField_1);
		
		JLabel lblSelectStayAlive = new JLabel("Training Method");
		lblSelectStayAlive.setFont(new Font("Tahoma", Font.BOLD, 17));
		lblSelectStayAlive.setHorizontalAlignment(SwingConstants.CENTER);
		lblSelectStayAlive.setBounds(10, 136, 178, 44);
		panel.add(lblSelectStayAlive);
		
	
		
		JLabel lblPowerUps = new JLabel("Power Ups");
		lblPowerUps.setHorizontalAlignment(SwingConstants.CENTER);
		lblPowerUps.setFont(new Font("Tahoma", Font.BOLD, 17));
		lblPowerUps.setBounds(198, 136, 146, 44);
		panel.add(lblPowerUps);
		
		JRadioButton rdbtnPowerSurge = new JRadioButton("Power Surge");
		rdbtnPowerSurge.setHorizontalAlignment(SwingConstants.LEFT);
		rdbtnPowerSurge.setBounds(208, 188, 127, 23);
		panel.add(rdbtnPowerSurge);
		
		JRadioButton rdbtnZapper = new JRadioButton("Zapper");
		rdbtnZapper.setHorizontalAlignment(SwingConstants.LEFT);
		rdbtnZapper.setBounds(208, 214, 127, 23);
		panel.add(rdbtnZapper);
		
		JRadioButton rdbtnConcurrentDamage = new JRadioButton("Concurrent Damage");
		rdbtnConcurrentDamage.setHorizontalAlignment(SwingConstants.LEFT);
		rdbtnConcurrentDamage.setBounds(208, 240, 127, 23);
		panel.add(rdbtnConcurrentDamage);
		
		JLabel lblOtherOptions = new JLabel("Other Options");
		lblOtherOptions.setHorizontalAlignment(SwingConstants.CENTER);
		lblOtherOptions.setFont(new Font("Tahoma", Font.BOLD, 17));
		lblOtherOptions.setBounds(354, 136, 194, 44);
		panel.add(lblOtherOptions);
		
		JRadioButton rdbtnStopWhenOut = new JRadioButton("Stop when out of OVLD");
		rdbtnStopWhenOut.setHorizontalAlignment(SwingConstants.LEFT);
		rdbtnStopWhenOut.setBounds(354, 188, 146, 23);
		panel.add(rdbtnStopWhenOut);
		
		JRadioButton rdbtnSpecOnlyOn = new JRadioButton("Spec only on PwrSurge");
		rdbtnSpecOnlyOn.setHorizontalAlignment(SwingConstants.LEFT);
		rdbtnSpecOnlyOn.setBounds(354, 214, 146, 23);
		panel.add(rdbtnSpecOnlyOn);
		
		JSeparator separator = new JSeparator();
		separator.setBounds(-1, 136, 549, 14);
		panel.add(separator);
		
		JSeparator separator_1 = new JSeparator();
		separator_1.setOrientation(SwingConstants.VERTICAL);
		separator_1.setBounds(197, 136, 5, 240);
		panel.add(separator_1);
		
		JSeparator separator_2 = new JSeparator();
		separator_2.setOrientation(SwingConstants.VERTICAL);
		separator_2.setBounds(349, 136, 5, 240);
		panel.add(separator_2);
		
		JLabel lblNewLabel_1 = new JLabel("New label");
		lblNewLabel_1.setBounds(10, 11, 538, 44);
		panel.add(lblNewLabel_1);
		
	
	}
}
