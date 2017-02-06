import java.awt.BorderLayout;
import java.awt.EventQueue;
import java.awt.Font;

import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JSeparator;
import javax.swing.JSpinner;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;
import javax.swing.SwingConstants;
import javax.swing.border.EmptyBorder;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;


public class NmzGui extends JFrame {

	private JPanel contentPane;
	private JTextField txtSpecPercent;
	private JTextField txtMaxHp;

	/**
	 * Launch the application.
	 */
	public static void main(String[] args) {
		EventQueue.invokeLater(new Runnable() {
			public void run() {
				try {
					NmzGui frame = new NmzGui();
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
	public NmzGui() {
		
		setTitle("NMZ Trainer");
		setAlwaysOnTop(true);
		setResizable(false);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 574, 460);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(null);
		setContentPane(contentPane);


		JRadioButton rdbtnMultiRun = new JRadioButton("Multiple Runs?");
		rdbtnMultiRun.setHorizontalAlignment(SwingConstants.LEFT);
		rdbtnMultiRun.setBounds(354, 241, 146, 23);
		contentPane.add(rdbtnMultiRun);
		
		JLabel lblNewLabel = new JLabel("Spec Percentage");
		lblNewLabel.setBounds(10, 83, 146, 14);
		contentPane.add(lblNewLabel);
		
		JLabel lblMaxHp = new JLabel("Max Hp");
		lblMaxHp.setBounds(10, 108, 146, 14);
		contentPane.add(lblMaxHp);
		

		
		JSpinner spnrPrayerPotion = new JSpinner();
		spnrPrayerPotion.setModel(new SpinnerNumberModel(1, 0, 28, 1));
		spnrPrayerPotion.setBounds(127, 241, 39, 20);
		contentPane.add(spnrPrayerPotion);
		
		JSpinner spnrOverloadPotion = new JSpinner();
		spnrOverloadPotion.setModel(new SpinnerNumberModel(8, 0, 28, 1));
		spnrOverloadPotion.setBounds(127, 272, 39, 20);
		contentPane.add(spnrOverloadPotion);
		
		JSpinner spnrAbsorptionPotion = new JSpinner();
		spnrAbsorptionPotion.setModel(new SpinnerNumberModel(12, 0, 28, 1));
		spnrAbsorptionPotion.setBounds(127, 303, 39, 20);
		contentPane.add(spnrAbsorptionPotion);
		
		JLabel lblPrayerPotion = new JLabel("Prayer Potion");
		lblPrayerPotion.setBounds(10, 244, 107, 14);
		contentPane.add(lblPrayerPotion);
		
		JLabel lblOverloadPotion = new JLabel("Overload Potion");
		lblOverloadPotion.setBounds(10, 275, 107, 14);
		contentPane.add(lblOverloadPotion);
		
		JLabel lblAbsorptionPotion = new JLabel("Absorption Potion");
		lblAbsorptionPotion.setBounds(10, 306, 107, 14);
		contentPane.add(lblAbsorptionPotion);
		
		JRadioButton rdbtnNewRadioButton = new JRadioButton("Prayer Method");
		rdbtnNewRadioButton.setHorizontalAlignment(SwingConstants.LEFT);
		rdbtnNewRadioButton.setBounds(10, 188, 146, 23);
		contentPane.add(rdbtnNewRadioButton);
		
		JRadioButton rdbtnAbsorptionMethod = new JRadioButton("Absorption Method");
		rdbtnAbsorptionMethod.setSelected(true);
		rdbtnAbsorptionMethod.setHorizontalAlignment(SwingConstants.LEFT);
		rdbtnAbsorptionMethod.setBounds(10, 214, 146, 23);
		contentPane.add(rdbtnAbsorptionMethod);
		
		txtSpecPercent = new JTextField();
		txtSpecPercent.setText("50");
		txtSpecPercent.setBounds(127, 80, 86, 20);
		contentPane.add(txtSpecPercent);
		txtSpecPercent.setColumns(10);
		
		txtMaxHp = new JTextField();
		txtMaxHp.setText("1");
		txtMaxHp.setColumns(10);
		txtMaxHp.setBounds(127, 105, 86, 20);
		contentPane.add(txtMaxHp);
		
		JLabel lblSelectStayAlive = new JLabel("Training Method");
		lblSelectStayAlive.setFont(new Font("Tahoma", Font.BOLD, 17));
		lblSelectStayAlive.setHorizontalAlignment(SwingConstants.CENTER);
		lblSelectStayAlive.setBounds(10, 136, 178, 44);
		contentPane.add(lblSelectStayAlive);
		
	
		
		JLabel lblPowerUps = new JLabel("Power Ups");
		lblPowerUps.setHorizontalAlignment(SwingConstants.CENTER);
		lblPowerUps.setFont(new Font("Tahoma", Font.BOLD, 17));
		lblPowerUps.setBounds(198, 136, 146, 44);
		contentPane.add(lblPowerUps);
		
		JRadioButton rdbtnPowerSurge = new JRadioButton("Power Surge");
		rdbtnPowerSurge.setSelected(true);
		rdbtnPowerSurge.setHorizontalAlignment(SwingConstants.LEFT);
		rdbtnPowerSurge.setBounds(208, 188, 127, 23);
		contentPane.add(rdbtnPowerSurge);
		
		JRadioButton rdbtnZapper = new JRadioButton("Zapper");
		rdbtnZapper.setSelected(true);
		rdbtnZapper.setHorizontalAlignment(SwingConstants.LEFT);
		rdbtnZapper.setBounds(208, 214, 127, 23);
		contentPane.add(rdbtnZapper);
		
		JRadioButton rdbtnConcurrentDamage = new JRadioButton("Concurrent Damage");
		rdbtnConcurrentDamage.setSelected(true);
		rdbtnConcurrentDamage.setHorizontalAlignment(SwingConstants.LEFT);
		rdbtnConcurrentDamage.setBounds(208, 240, 127, 23);
		contentPane.add(rdbtnConcurrentDamage);
		
		JLabel lblOtherOptions = new JLabel("Other Options");
		lblOtherOptions.setHorizontalAlignment(SwingConstants.CENTER);
		lblOtherOptions.setFont(new Font("Tahoma", Font.BOLD, 17));
		lblOtherOptions.setBounds(354, 136, 194, 44);
		contentPane.add(lblOtherOptions);
		
		JRadioButton rdbtnStopWhenOut = new JRadioButton("Stop when out of OVLD");
		rdbtnStopWhenOut.setSelected(true);
		rdbtnStopWhenOut.setHorizontalAlignment(SwingConstants.LEFT);
		rdbtnStopWhenOut.setBounds(354, 188, 146, 23);
		contentPane.add(rdbtnStopWhenOut);
		
		JRadioButton rdbtnSpecOnlyOn = new JRadioButton("Spec only on PwrSurge");
		rdbtnSpecOnlyOn.setSelected(true);
		rdbtnSpecOnlyOn.setHorizontalAlignment(SwingConstants.LEFT);
		rdbtnSpecOnlyOn.setBounds(354, 214, 146, 23);
		contentPane.add(rdbtnSpecOnlyOn);
		
		JSeparator separator = new JSeparator();
		separator.setBounds(-1, 136, 549, 14);
		contentPane.add(separator);
		
		JSeparator separator_1 = new JSeparator();
		separator_1.setOrientation(SwingConstants.VERTICAL);
		separator_1.setBounds(197, 136, 5, 240);
		contentPane.add(separator_1);
		
		JSeparator separator_2 = new JSeparator();
		separator_2.setOrientation(SwingConstants.VERTICAL);
		separator_2.setBounds(349, 136, 5, 240);
		contentPane.add(separator_2);
		
		JLabel lblNewLabel_1 = new JLabel("NMZ Trainer");
		lblNewLabel_1.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_1.setFont(new Font("Tahoma", Font.BOLD, 16));
		lblNewLabel_1.setBounds(10, 11, 538, 44);
		contentPane.add(lblNewLabel_1);
		
		JButton btnNewButton = new JButton("Start!");
		btnNewButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
			}
		});
		btnNewButton.setBounds(10, 375, 538, 36);
		contentPane.add(btnNewButton);
		

	}
}
