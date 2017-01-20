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


    public Gui(final ScriptVars var) {
        setTitle("GB Feather Buyer");
        setIconImage(Toolkit.getDefaultToolkit().getImage(Gui.class.getResource("/javax/swing/plaf/metal/icons/ocean/computer.gif")));
        setAlwaysOnTop(true);
        setResizable(false);
        setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        setBounds(100, 100, 276, 400);
        contentPane = new JPanel();
        contentPane.setBackground(Color.WHITE);
        contentPane.setBorder(new EmptyBorder(5, 5, 5, 5));
        setContentPane(contentPane);
        contentPane.setLayout(null);

        JPanel panel = new JPanel();
        panel.setBounds(0, 0, 270, 300);
        contentPane.add(panel);
        panel.setLayout(null);

        JLabel lblMinSpecPercent = new JLabel("Min Spec Percent: ");
        lblMinSpecPercent.setBounds(0, 10, 150, 15);
        panel.add(lblMinSpecPercent);

        txtMinSpecPercent = new JTextField();
        txtMinSpecPercent.setBounds(170, 10, 90, 15);
        panel.add(txtMinSpecPercent);
        txtMinSpecPercent.setColumns(10);

        JLabel lblMaxHp = new JLabel("Keep HP Below");
        lblMaxHp.setBounds(0,40, 150, 15);
        panel.add(lblMaxHp);

        txtMaxHp = new JTextField();
        txtMaxHp.setBounds(170,40,90,15);
        txtMaxHp.setText("99");
        panel.add(txtMaxHp);
        txtMaxHp.setColumns(10);




    }

}
