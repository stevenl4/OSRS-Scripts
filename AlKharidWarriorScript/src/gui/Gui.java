package gui;

import util.ScriptVars;

import javax.swing.*;
import javax.swing.border.EmptyBorder;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by Steven on 1/21/2017.
 */
public class Gui extends JFrame {
    private JPanel contentPane;
    private JTextField txtFoodName;
    private JTextField txtFoodAmt;
//    JCheckBox chckbxHopWorlds = new JCheckBox("hop worlds");

    public Gui(final ScriptVars var){

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
        panel.setBounds(0, 0, 400, 412);
        contentPane.add(panel);
        panel.setLayout(null);

        // Food Name
        JLabel lblFoodName = new JLabel("Food Name:");
        lblFoodName.setBounds(0, 10, 90, 15);
        panel.add(lblFoodName);

        txtFoodName = new JTextField();
        txtFoodName.setText("Tuna");
        txtFoodName.setBounds(100, 10, 150, 20);
        panel.add(txtFoodName);
        txtFoodName.setColumns(10);

        // Food Amount
        JLabel lblFoodAmt = new JLabel("Food Amount:");
        lblFoodAmt.setBounds(0, 35, 90, 15);
        panel.add(lblFoodAmt);

        txtFoodAmt = new JTextField();
        txtFoodAmt.setText("0");
        txtFoodAmt.setBounds(100, 35, 150, 20);
        panel.add(txtFoodAmt);
        txtFoodAmt.setColumns(10);


//        JLabel lblHopWorlds = new JLabel("Hop Worlds:");
//        lblHopWorlds.setBounds(0, 60, 90, 15);
//        panel.add(lblHopWorlds);
//
//        chckbxHopWorlds.setBounds(100, 60, 100, 20);
//        chckbxHopWorlds.setSelected(true);
//        panel.add(chckbxHopWorlds);

        JButton btnNewButton = new JButton("Start!");
        btnNewButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {

                var.foodName = txtFoodName.getText();
                var.requiredFoodAmt = Integer.parseInt(txtFoodAmt.getText());
                var.started = true;
                dispose();
            }
        });

        btnNewButton.setBounds(0,421,400,50);
        contentPane.add(btnNewButton);
    }
}