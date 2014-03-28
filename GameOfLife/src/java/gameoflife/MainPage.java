package java.gameoflife;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by devan on 3/22/14.
 */
public class MainPage {
    private JPanel panelMain;
    private JLabel josephConwayGameLabel;
    private JButton startButton;
    private JButton stopButton;
    private JTable table1;
    private int numClicks = 0;

    public MainPage() {

        startButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                numClicks++;
                if (numClicks / 2 == 0){
                    startButton.setText("Start");
                } else{
                    startButton.setText("Pause");
                }
            }
        });
    }

    private void createUIComponents() {
        // TODO: place custom component creation code here
    }
}
