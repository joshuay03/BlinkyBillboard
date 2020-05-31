package ControlPanel;

import BillboardSupport.Billboard;
import Client.ClientConnector;
import SocketCommunication.Request;
import SocketCommunication.Response;

import javax.swing.*;
import javax.swing.text.DefaultFormatterFactory;
import javax.swing.text.NumberFormatter;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.PropertyChangeEvent;
import java.beans.PropertyChangeListener;
import java.io.IOException;
import java.sql.Timestamp;
import java.text.DecimalFormat;
import java.text.NumberFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.time.LocalDateTime;
import java.util.List;


/**
 * This class allows a user to view the schedule for all the billboards which are currently scheduled.
 */
public class Schedule {
    protected JPanel schedulePanel;
    protected JPanel datePanel;
    protected JLabel dateLabel;
    protected JTextField dateTextField;
    protected JButton selectDateButton;
    protected JComboBox hourComboBox;
    protected JComboBox minuteComboBox;
    protected JFormattedTextField durationFormattedTextField;
    protected JLabel selectDurationLabel;
    protected JPanel timeDurationPanel;
    protected JRadioButton dailyRadioButton;
    protected JRadioButton customMinutesRadioButton;
    protected JRadioButton hourlyRadioButton;
    protected JPanel frequencyPanel;
    protected JFormattedTextField customFrequencyFormattedTextField;
    private JButton doneButton;
    private JLabel billboardName;
    private JComboBox<String> billboardNames;
    protected DefaultComboBoxModel<String> model;

    /**
     * Create a view of the currently scheduled billboards.
     * @param scheduleFrame The frame which contains the schedule
     * @param connector A ClientConnector object
     * @param billboards A list of all billboards containec within the database.
     */
    Schedule(JFrame scheduleFrame, ClientConnector connector, List<Billboard> billboards) {

        model = new DefaultComboBoxModel<>();

        billboards.forEach(x ->
                model.addElement(x.getBillboardName())
        );

        billboardNames.setModel(model);


        hourComboBox.addItem("Select Hour");
        for (int i = 0; i <= 23; i++) {
            if (i < 10) {
                hourComboBox.addItem("0" + i);
            } else {
                hourComboBox.addItem(i);
            }
        }

        minuteComboBox.addItem("Select Minute");
        for (int i = 0; i <= 59; i++) {
            if (i < 10) {
                minuteComboBox.addItem("0" + i);
            } else {
                minuteComboBox.addItem(i);
            }
        }

        NumberFormat numFormat = new DecimalFormat("#");

        NumberFormatter durationFormatter = new NumberFormatter(numFormat);
        durationFormatter.setMinimum(1);
        DefaultFormatterFactory durationFactory = new DefaultFormatterFactory(durationFormatter);
        durationFormattedTextField.setFormatterFactory(durationFactory);
        durationFormattedTextField.setValue(1);

        NumberFormatter customFrequencyFormatter = new NumberFormatter(numFormat);
        customFrequencyFormatter.setMinimum(1);
        customFrequencyFormatter.setMaximum((int) durationFormattedTextField.getValue());
        DefaultFormatterFactory customFrequencyFactory = new DefaultFormatterFactory(customFrequencyFormatter);
        customFrequencyFormattedTextField.setFormatterFactory(customFrequencyFactory);
        customFrequencyFormattedTextField.setValue(1);

        selectDateButton.addActionListener(new ActionListener() {
            /**
             * Invoked when an action occurs.
             * Set the date given by the user.
             * @param e the event to be processed
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                dateTextField.setText(new DatePicker(scheduleFrame).setPickedDate());
            }
        });

        durationFormattedTextField.addPropertyChangeListener(new PropertyChangeListener() {
            /**
             *  
             * @param evt the event to be processed
             */
            @Override
            public void propertyChange(PropertyChangeEvent evt) {
                customFrequencyFormatter.setMaximum((int) durationFormattedTextField.getValue());
                DefaultFormatterFactory customFrequencyFactory = new DefaultFormatterFactory(customFrequencyFormatter);
                customFrequencyFormattedTextField.setFormatterFactory(customFrequencyFactory);
            }
        });

        dailyRadioButton.addActionListener(new ActionListener() {
            /**
             * Invoked when an action occurs.
             * Set the schedule time by the day.
             * @param e the event to be processed
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                hourlyRadioButton.setSelected(false);
                customMinutesRadioButton.setSelected(false);
            }
        });

        hourlyRadioButton.addActionListener(new ActionListener() {
            /**
             * Invoked when an action occurs.
             * Set the schedule time by the hour.
             * @param e the event to be processed
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                dailyRadioButton.setSelected(false);
                customMinutesRadioButton.setSelected(false);
            }
        });

        customMinutesRadioButton.addActionListener(new ActionListener() {
            /**
             * Invoked when an action occurs.
             * Set the schedule time by the minute.
             * @param e the event to be processed
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                dailyRadioButton.setSelected(false);
                hourlyRadioButton.setSelected(false);
            }
        });
        doneButton.addActionListener(new ActionListener() {
            /**
             * Invoked when an action occurs.
             * When the user is done entering their scheduling details, schedule the billboard.
             * @param e the event to be processed
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                String[] date = dateTextField.getText().split("-");
                int hour;
                int minute;
                int duration = Integer.parseInt(durationFormattedTextField.getText());
                try{
                    hour = Integer.parseInt(String.valueOf(hourComboBox.getSelectedItem()));
                    minute = Integer.parseInt(String.valueOf(minuteComboBox.getSelectedItem()));
                } catch (NumberFormatException ex){
                    JOptionPane.showMessageDialog(null, "You must select an hour and minute to continue.");
                    return;
                }

                SimpleDateFormat format = new SimpleDateFormat("dd-MM-yyyy");

                try {
                    format.parse(dateTextField.getText());
                } catch (ParseException ex) {
                    JOptionPane.showMessageDialog(null, "Improper entry for date. Please enter in format: dd-mm-yyyy");
                    return;
                }


                int frequency;
                if (dailyRadioButton.isSelected()) {
                    frequency = 24 * 60;
                } else if (hourlyRadioButton.isSelected()) {
                    frequency = 60;
                } else {
                    frequency = Integer.parseInt(customFrequencyFormattedTextField.getText());
                }

                Timestamp timestamp = Timestamp.valueOf(LocalDateTime.of(Integer.parseInt(date[2]), Integer.parseInt(date[1]), Integer.parseInt(date[0]), hour, minute));


                BillboardSupport.Schedule schedule = new BillboardSupport.Schedule(timestamp, duration, frequency, (String) billboardNames.getSelectedItem(), Timestamp.valueOf(LocalDateTime.now()));
                Request scheduleBillboardReq = Request.scheduleBillboardReq(schedule, connector.session);

                Response response;

                try {
                    response = scheduleBillboardReq.Send(connector);
                } catch (IOException excep) {
                    JOptionPane.showMessageDialog(null, "Cannot connect to server");
                    return;
                }

                // check status of response
                boolean status = response.isStatus();

                if (!status) {
                    String errorMsg = (String) response.getData();
                    JOptionPane.showMessageDialog(null, errorMsg);
                    return;
                }

                JOptionPane.showMessageDialog(null, "Successfully scheduled billboard.");
                scheduleFrame.setVisible(false);
            }
        });
    }
}
