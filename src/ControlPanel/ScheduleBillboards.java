package ControlPanel;

import Client.ClientConnector;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

/**
 * A class to represent a "Schedule Billboards" page which is bound to ScheduleBillboards.form
 */
public class ScheduleBillboards {
    protected JPanel scheduleBillboardsPanel;
    protected JPanel schedulePanel;
    protected JLabel scheduledDateLabel;
    protected JTable scheduleTable;
    protected JPanel titlePanel;
    protected JButton backButton;
    protected JButton scheduleButton;

    protected JFrame scheduleFrame;

    /**
     *
     * @param frame
     */
    public ScheduleBillboards(JFrame frame, ClientConnector connector) {
        backButton.addActionListener(new ActionListener() {
            /**
             * Invoked when an action occurs.
             *
             * @param e the event to be processed
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                frame.setContentPane(new OptionMenu(frame, connector).optionMenuPanel);
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);
            }
        });

        scheduleButton.addActionListener(new ActionListener() {
            @Override
            public void actionPerformed(ActionEvent e) {
                scheduleFrame.setContentPane(new Schedule(scheduleFrame, connector).schedulePanel);
                scheduleFrame.pack();
                scheduleFrame.setVisible(true);
            }
        });
    }

    /**
     *
     */
    private void createUIComponents() {
        final int daysInAWeek = 7;
        String[] daysOfTheWeek = new String[daysInAWeek];

        SimpleDateFormat simpleDateformat = new SimpleDateFormat("EEEE");
        Calendar calendar = Calendar.getInstance();

        for (int i = 0; i < daysInAWeek; i++) {
            daysOfTheWeek[i] = simpleDateformat.format(calendar.getTime());
            calendar.add(Calendar.DAY_OF_YEAR, 1);
        }

        Object[][] scheduledBillboards = new Object[][] {
                daysOfTheWeek,
                {"Billboard Name", "Billboard Name", "Billboard Name", "Billboard Name", "Billboard Name", "Billboard Name", "Billboard Name"}
        };

        DefaultTableModel model = new DefaultTableModel(scheduledBillboards, daysOfTheWeek);
        scheduleTable = new JTable(model);

        scheduleFrame = new JFrame("Schedule a Billboard");
        scheduleFrame.setPreferredSize(new Dimension(600, 300));
    }
}

