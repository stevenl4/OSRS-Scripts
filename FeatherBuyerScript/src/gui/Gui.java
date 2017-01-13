package gui;

import util.ScriptVars;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.border.EmptyBorder;

import java.awt.Toolkit;

import javax.swing.JLabel;
import javax.swing.JTextField;
import javax.swing.JCheckBox;
import javax.swing.JButton;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.awt.Color;

public class Gui extends JFrame {

    /**
     *
     */
    private static final long serialVersionUID = 1L;
    private JPanel contentPane;

    private JTextField txtItemName;
    private JTextField txtShopId;
    private JTextField txtPerItem;
    JCheckBox chckbxHopWorlds = new JCheckBox("hop worlds");
    private JTextField txtMinAmt;
    private JTextField txtMinGpAmt;
    private JTextField txtMinBuySleep;
    private JTextField txtMaxBuySleep;
    private JTextField txtMinOpenSleep;
    private JTextField txtMaxOpenSleep;

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

        JLabel lblItemName = new JLabel("Item Name:");
        lblItemName.setBounds(0, 11, 93, 14);
        panel.add(lblItemName);

        txtItemName = new JTextField();
        txtItemName.setText("Feather");
        txtItemName.setBounds(96, 9, 132, 20);
        panel.add(txtItemName);
        txtItemName.setColumns(10);

        txtShopId = new JTextField();
        txtShopId.setText("1027");
        txtShopId.setColumns(10);
        txtShopId.setBounds(96, 36, 132, 20);
        panel.add(txtShopId);

        JLabel lblShopId = new JLabel("Shop Id:");
        lblShopId.setBounds(0, 38, 93, 14);
        panel.add(lblShopId);

        txtPerItem = new JTextField();
        txtPerItem.setText("300");
        txtPerItem.setColumns(10);
        txtPerItem.setBounds(96, 67, 39, 20);
        panel.add(txtPerItem);

        JLabel lblPerItem = new JLabel("Per Item:");
        lblPerItem.setBounds(0, 69, 93, 14);
        panel.add(lblPerItem);

        JLabel lblHopWorlds = new JLabel("Hop Worlds:");
        lblHopWorlds.setBounds(0, 94, 73, 14);
        panel.add(lblHopWorlds);

        chckbxHopWorlds.setBounds(96, 90, 97, 23);
        chckbxHopWorlds.setSelected(true);
        panel.add(chckbxHopWorlds);

        JLabel lblMinimumAmt = new JLabel("Minimum Amt:");
        lblMinimumAmt.setBounds(0, 121, 93, 14);
        panel.add(lblMinimumAmt);

        txtMinAmt = new JTextField();
        txtMinAmt.setText("75");
        txtMinAmt.setBounds(96, 118, 39, 20);
        panel.add(txtMinAmt);
        txtMinAmt.setColumns(10);

        JLabel lblMinGpAmt = new JLabel("Min GP Required: ");
        lblMinGpAmt.setBounds(0, 146, 93, 14);
        panel.add(lblMinGpAmt);

        txtMinGpAmt = new JTextField();
        txtMinGpAmt.setText("1000");
        txtMinGpAmt.setBounds(96,146,39,20);
        panel.add(txtMinGpAmt);
        txtMinGpAmt.setColumns(10);

        // Timeout sleep
        JLabel lblBuySleep = new JLabel("Buy Timeouts: ");
        lblBuySleep.setBounds(0, 180, 93, 14);
        panel.add(lblBuySleep);

        txtMinBuySleep = new JTextField();
        txtMinBuySleep.setText("100");
        txtMinBuySleep.setBounds(96,175,39,20);
        panel.add(txtMinBuySleep);
        txtMinBuySleep.setColumns(10);

        txtMaxBuySleep = new JTextField();
        txtMaxBuySleep.setText("200");
        txtMaxBuySleep.setBounds(160,175,39,20);
        panel.add(txtMaxBuySleep);
        txtMaxBuySleep.setColumns(10);

        // Open Sleep
        JLabel lblOpenSleep = new JLabel("Open Sleep Timer: ");
        lblOpenSleep.setBounds(0, 220, 93, 14);
        panel.add(lblOpenSleep);

        txtMinOpenSleep = new JTextField();
        txtMinOpenSleep.setText("50");
        txtMinOpenSleep.setBounds(96,215,39,20);
        panel.add(txtMinOpenSleep);
        txtMinOpenSleep.setColumns(10);

        txtMaxOpenSleep = new JTextField();
        txtMaxOpenSleep.setText("100");
        txtMaxOpenSleep.setBounds(160,215,39,20);
        panel.add(txtMaxOpenSleep);
        txtMaxOpenSleep.setColumns(10);

        JButton btnNewButton = new JButton("Start!");
        btnNewButton.addActionListener(new ActionListener() {
            public void actionPerformed(ActionEvent arg0) {
                var.shopId = Integer.parseInt(txtShopId.getText());
                var.itemName =  txtItemName.getText();
                var.packName =  txtItemName.getText() + " pack";
                var.perItem = Integer.parseInt(txtPerItem.getText());
                var.hopWorlds = chckbxHopWorlds.isSelected();
                var.minAmt = Integer.parseInt(txtMinAmt.getText());
                var.minGP = Integer.parseInt(txtMinGpAmt.getText());
                var.minBuySleep =Integer.parseInt(txtMinBuySleep.getText());
                var.maxBuySleep = Integer.parseInt(txtMaxBuySleep.getText());
                var.minOpenSleep = Integer.parseInt(txtMinOpenSleep.getText());
                var.maxOpenSleep = Integer.parseInt(txtMaxOpenSleep.getText());
                var.started = true;
                dispose();
            }
        });
        btnNewButton.setBounds(0, 300, 270, 34);
        contentPane.add(btnNewButton);
    }
}