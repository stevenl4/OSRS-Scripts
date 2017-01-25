package gui;

import util.ScriptVars;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by steven.luo on 19/01/2017.
 */


public class Gui extends JFrame {
    private JPanel contentPane;

    private JTextField txtMinSpecPercent;
    private JTextField txtMaxHp;
    JCheckBox chckbxPrayerMethod = new JCheckBox("Protect Prayer and use prayer pots");
    JCheckBox chckbxAbsorptionMethod = new JCheckBox("Lower HP and use absorption");
    JCheckBox chckbxExitOutOfOverload = new JCheckBox("Exit when out of overloads");
    JCheckBox chckbxUseSpecialOnlyOnPowerUp = new JCheckBox("Use Special Only on Power up");
    JCheckBox chckbxTestMode = new JCheckBox("Test Mode");

    public Gui(final ScriptVars var) {
        setTitle("DreamBot");
        setAlwaysOnTop(true);
        setResizable(false);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 400, 500);
        contentPane = new JPanel();
        contentPane.setBackground(Color.WHITE);
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        JPanel panel = new JPanel();
        panel.setBounds(0, 0, 400, 420);
        contentPane.add(panel);
        panel.setLayout(null);

        // Min Spec
        JLabel lblMinSpecPercent = new JLabel("Min Spec Percent: ");
        lblMinSpecPercent.setBounds(0, 10, 150, 15);
        panel.add(lblMinSpecPercent);

        txtMinSpecPercent = new JTextField();
        txtMinSpecPercent.setText("25");
        txtMinSpecPercent.setBounds(170, 10, 150, 20);
        panel.add(txtMinSpecPercent);
        txtMinSpecPercent.setColumns(10);

        // Max HP
        JLabel lblMaxHp = new JLabel("Keep HP Below");
        lblMaxHp.setBounds(0,35, 150, 15);
        panel.add(lblMaxHp);

        txtMaxHp = new JTextField();
        txtMaxHp.setBounds(170,35,150,20);
        txtMaxHp.setText("1");
        panel.add(txtMaxHp);
        txtMaxHp.setColumns(10);

        // Prayer Method
        JLabel lblPrayerMethod = new JLabel("Prayer Method:");
        lblPrayerMethod.setBounds(0, 60, 150, 15);
        panel.add(lblPrayerMethod);

        chckbxPrayerMethod.setBounds(170, 60, 150, 20);
        chckbxPrayerMethod.setSelected(false);
        panel.add(chckbxPrayerMethod);

        // Absorption Method
        JLabel lblAbsorptionMethod = new JLabel("Absorption Method:");
        lblAbsorptionMethod.setBounds(0,85,150,15);
        panel.add(lblAbsorptionMethod);

        chckbxAbsorptionMethod.setBounds(170, 85, 150, 20);
        chckbxAbsorptionMethod.setSelected(true);
        panel.add(chckbxAbsorptionMethod);

        // Exit when out of overloads
        JLabel lblExitOutOfOverload = new JLabel("Exit when out of Overloads:");
        lblExitOutOfOverload.setBounds(0,110,150,15);
        panel.add(lblExitOutOfOverload);

        chckbxExitOutOfOverload.setBounds(170,110,150,20);
        chckbxExitOutOfOverload.setSelected(true);
        panel.add(chckbxExitOutOfOverload);

        JLabel lblUseSpecialOnlyOnPowerUp = new JLabel("Special Usage:");
        lblUseSpecialOnlyOnPowerUp.setBounds(0, 135, 150, 15);
        panel.add(lblUseSpecialOnlyOnPowerUp);

        chckbxUseSpecialOnlyOnPowerUp.setBounds(170,135,150,20);
        chckbxUseSpecialOnlyOnPowerUp.setSelected(false);
        panel.add(chckbxUseSpecialOnlyOnPowerUp);

        // TestMode
        chckbxTestMode.setBounds(0,400,150,20);
        chckbxTestMode.setSelected(false);
        panel.add(chckbxTestMode);
        JButton btnNewButton = new JButton("Start!");
        btnNewButton.addActionListener(e -> {

            var.specMinPercent = Integer.parseInt(txtMinSpecPercent.getText());
            var.maxHp = Integer.parseInt(txtMaxHp.getText());
            var.prayerMethod = chckbxPrayerMethod.isSelected();
            var.absorptionMethod = chckbxAbsorptionMethod.isSelected();
            var.started = true;
            var.testMode = chckbxTestMode.isSelected();
            var.exitWhenOutOfOverload = chckbxExitOutOfOverload.isSelected();
            var.useSpecialOnlyOnPowerUp = chckbxUseSpecialOnlyOnPowerUp.isSelected();
            dispose();
        });

        btnNewButton.setBounds(0,421,400,50);
        contentPane.add(btnNewButton);
    }

}
