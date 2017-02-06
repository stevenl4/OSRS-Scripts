package gui;

import util.ScriptVars;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;


public class NmzGui extends JFrame {

	private JPanel contentPane;
	private JTextField txtSpecPercent;
	private JTextField txtMaxHp;


	/**
	 * Create the frame.
	 */
	public NmzGui(final ScriptVars var) {
		
		setTitle("NMZ Trainer");
		setAlwaysOnTop(true);
		setResizable(false);
		setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
		setBounds(100, 100, 574, 460);
		contentPane = new JPanel();
		contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
		contentPane.setLayout(null);
		setContentPane(contentPane);

		JLabel lblNewLabel_1 = new JLabel("NMZ Trainer");
		lblNewLabel_1.setHorizontalAlignment(SwingConstants.CENTER);
		lblNewLabel_1.setFont(new Font("Tahoma", Font.BOLD, 16));
		lblNewLabel_1.setBounds(10, 11, 538, 44);
		contentPane.add(lblNewLabel_1);


		
		JLabel lblNewLabel = new JLabel("Spec Percentage");
		lblNewLabel.setBounds(10, 83, 146, 14);
		contentPane.add(lblNewLabel);

		txtSpecPercent = new JTextField();
		txtSpecPercent.setText("50");
		txtSpecPercent.setBounds(127, 80, 86, 20);
		contentPane.add(txtSpecPercent);
		txtSpecPercent.setColumns(10);

		JLabel lblMaxHp = new JLabel("Max Hp");
		lblMaxHp.setBounds(10, 108, 146, 14);
		contentPane.add(lblMaxHp);

		txtMaxHp = new JTextField();
		txtMaxHp.setText("1");
		txtMaxHp.setColumns(10);
		txtMaxHp.setBounds(127, 105, 86, 20);
		contentPane.add(txtMaxHp);
		
		JSpinner spnrPrayerPotion = new JSpinner();
		spnrPrayerPotion.setModel(new SpinnerNumberModel(1, 0, 28, 1));
		spnrPrayerPotion.setBounds(127, 241, 39, 20);
		contentPane.add(spnrPrayerPotion);

		JLabel lblPrayerPotion = new JLabel("Prayer Potion");
		lblPrayerPotion.setBounds(10, 244, 107, 14);
		contentPane.add(lblPrayerPotion);
		
		JSpinner spnrOverloadPotion = new JSpinner();
		spnrOverloadPotion.setModel(new SpinnerNumberModel(8, 0, 28, 1));
		spnrOverloadPotion.setBounds(127, 272, 39, 20);
		contentPane.add(spnrOverloadPotion);

		JLabel lblOverloadPotion = new JLabel("Overload Potion");
		lblOverloadPotion.setBounds(10, 275, 107, 14);
		contentPane.add(lblOverloadPotion);
		
		JSpinner spnrAbsorptionPotion = new JSpinner();
		spnrAbsorptionPotion.setModel(new SpinnerNumberModel(12, 0, 28, 1));
		spnrAbsorptionPotion.setBounds(127, 303, 39, 20);
		contentPane.add(spnrAbsorptionPotion);
		
		JLabel lblAbsorptionPotion = new JLabel("Absorption Potion");
		lblAbsorptionPotion.setBounds(10, 306, 107, 14);
		contentPane.add(lblAbsorptionPotion);

		JLabel lblSelectStayAlive = new JLabel("Training Method");
		lblSelectStayAlive.setFont(new Font("Tahoma", Font.BOLD, 17));
		lblSelectStayAlive.setHorizontalAlignment(SwingConstants.CENTER);
		lblSelectStayAlive.setBounds(10, 136, 178, 44);
		contentPane.add(lblSelectStayAlive);

		JRadioButton rdbtnPrayerMethod = new JRadioButton("Prayer Method");
		rdbtnPrayerMethod.setHorizontalAlignment(SwingConstants.LEFT);
		rdbtnPrayerMethod.setBounds(10, 188, 146, 23);
		contentPane.add(rdbtnPrayerMethod);

		JRadioButton rdbtnAbsorptionMethod = new JRadioButton("Absorption Method");
		rdbtnAbsorptionMethod.setSelected(true);
		rdbtnAbsorptionMethod.setHorizontalAlignment(SwingConstants.LEFT);
		rdbtnAbsorptionMethod.setBounds(10, 214, 146, 23);
		contentPane.add(rdbtnAbsorptionMethod);

		
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

		JRadioButton rdbtnMultiRun = new JRadioButton("Multiple Runs?");
		rdbtnMultiRun.setHorizontalAlignment(SwingConstants.LEFT);
		rdbtnMultiRun.setBounds(354, 241, 146, 23);
		contentPane.add(rdbtnMultiRun);

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
		

		
		JButton btnNewButton = new JButton("Start!");
		btnNewButton.addActionListener(new ActionListener() {
			@Override
			public void actionPerformed(ActionEvent e) {
				var.maxHp = Integer.parseInt(txtMaxHp.getText());
				var.specMinPercent = Integer.parseInt(txtSpecPercent.getText());
				var.exitWhenOutOfOverload = rdbtnStopWhenOut.isSelected();
				var.useSpecialOnlyOnPowerUp = rdbtnSpecOnlyOn.isSelected();
				var.prayerMethod = rdbtnPrayerMethod.isSelected();
				var.absorptionMethod = rdbtnAbsorptionMethod.isSelected();
				var.usePowerSurge = rdbtnPowerSurge.isSelected();
				var.useConcurrentDamage = rdbtnConcurrentDamage.isSelected();
				var.useZapper = rdbtnZapper.isSelected();
				var.prayerPotionAmt = Integer.parseInt(spnrPrayerPotion.getValue().toString());
				var.absorptionPotionAmt = Integer.parseInt(spnrAbsorptionPotion.getValue().toString());
				var.overloadPotionAmt = Integer.parseInt(spnrOverloadPotion.getValue().toString());
				var.multiRun = rdbtnMultiRun.isSelected();
				var.started = true;
				dispose();
			}
		});
		btnNewButton.setBounds(10, 375, 538, 36);
		contentPane.add(btnNewButton);
		

	}
}
