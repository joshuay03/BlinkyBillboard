package ControlPanel;

import BillboardSupport.Billboard;
import BillboardSupport.RenderedBillboard;
import Client.ClientConnector;
import SocketCommunication.Request;
import org.w3c.dom.Document;
import org.w3c.dom.NodeList;

import javax.imageio.ImageIO;
import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.image.BufferedImage;
import java.awt.image.DataBufferByte;
import java.awt.image.WritableRaster;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.time.LocalDateTime;
import java.util.Base64;

import static SocketCommunication.ServerRequest.CREATE_BILLBOARD;

/**
 * A class to represent a "Create Billboards" page which is bound to CreateBillboards.form
 * @author Joshua Young
 */
public class CreateBillboards {
    protected JPanel createBillboardsPanel;
    protected JButton importButton;
    protected JButton exportButton;
    protected JLabel createBillboardsLabel;
    protected JPanel optionPanel;
    protected JPanel createPanel;
    protected JLabel messageLabel;
    protected JTextArea messageTextArea;
    protected JButton messageColourButton;
    protected JLabel pictureURLLabel;
    protected JFormattedTextField pictureURLFormattedTextField;
    protected JLabel informationLabel;
    protected JTextArea informationTextArea;
    protected JButton informationColourButton;
    protected JButton backgroundColourButton;
    protected JButton viewBillboardButton;
    private JButton backButton;
    protected JPanel titlePanel;
    private JButton pictureButton;

    protected Billboard billboard;
    protected Color backgroundColour = null;
    protected String messageText = null;
    protected Color messageColor = null;
    protected URL pictureURL = null;
    protected byte[] pictureData = null;
    protected String informationText = null;
    protected Color informationColor = null;

    protected ColourChooser colourChooser = new ColourChooser();

    /**
     *
     * @param frame
     */
    public CreateBillboards(JFrame frame, ClientConnector connector) {
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

        importButton.addActionListener(new ActionListener() {
            /**
             * Invoked when an action occurs.
             *
             * @param e the event to be processed
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                FileNameExtensionFilter filter = new FileNameExtensionFilter("XML", "xml");
                chooser.setFileFilter(filter);
                int returnVal = chooser.showOpenDialog(null);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File xmlFile = chooser.getSelectedFile();
                    importXML(xmlFile);
                }
            }
        });

        messageTextArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                messageText = messageTextArea.getText();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                messageText = messageTextArea.getText();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                messageText = messageTextArea.getText();
            }
        });

        messageColourButton.addActionListener(new ActionListener() {
            /**
             * Invoked when an action occurs.
             *
             * @param e the event to be processed
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                int res = JOptionPane.showOptionDialog(null, colourChooser, "Choose colour", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);
                if (res == 0) {
                    messageColor = colourChooser.getColor();
                }
            }
        });

        pictureButton.addActionListener(new ActionListener() {
            /**
             * Invoked when an action occurs.
             *
             * @param e the event to be processed
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                JFileChooser chooser = new JFileChooser();
                FileNameExtensionFilter filter = new FileNameExtensionFilter("Pictures", "bmp", "jpeg", "png");
                chooser.setFileFilter(filter);
                int returnVal = chooser.showOpenDialog(null);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File pictureFile = chooser.getSelectedFile();
                    try {
                        pictureData = Base64.getDecoder().decode(encodeFileToBase64Binary(pictureFile).getBytes());
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
            }
        });

        pictureURLFormattedTextField.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                try {
                    pictureURL = new URL(pictureURLFormattedTextField.getText());
                } catch (MalformedURLException malformedURLException) {
                    malformedURLException.printStackTrace();
                }
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                try {
                    pictureURL = new URL(pictureURLFormattedTextField.getText());
                } catch (MalformedURLException malformedURLException) {
                    malformedURLException.printStackTrace();
                }
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                try {
                    pictureURL = new URL(pictureURLFormattedTextField.getText());
                } catch (MalformedURLException malformedURLException) {
                    malformedURLException.printStackTrace();
                }
            }
        });

        informationTextArea.getDocument().addDocumentListener(new DocumentListener() {
            @Override
            public void insertUpdate(DocumentEvent e) {
                informationText = informationTextArea.getText();
            }

            @Override
            public void removeUpdate(DocumentEvent e) {
                informationText = informationTextArea.getText();
            }

            @Override
            public void changedUpdate(DocumentEvent e) {
                informationText = informationTextArea.getText();
            }
        });

        informationColourButton.addActionListener(new ActionListener() {
            /**
             * Invoked when an action occurs.
             *
             * @param e the event to be processed
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                int res = JOptionPane.showOptionDialog(null, colourChooser, "Choose colour", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);
                if (res == 0) {
                    informationColor = colourChooser.getColor();
                }
            }
        });

        backgroundColourButton.addActionListener(new ActionListener() {
            /**
             * Invoked when an action occurs.
             *
             * @param e the event to be processed
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                int res = JOptionPane.showOptionDialog(null, colourChooser, "Choose colour", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);
                if (res == 0) {
                    backgroundColour = colourChooser.getColor();
                }
            }
        });

        viewBillboardButton.addActionListener(new ActionListener() {
            /**
             * Invoked when an action occurs.
             *
             * @param e the event to be processed
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                if (pictureURL == null) {
                    billboard = new Billboard(backgroundColour, messageColor, informationColor, messageText, informationText, new ImageIcon(pictureData), LocalDateTime.now(), 30, 5);
                }
                else {
                    billboard = new Billboard(backgroundColour, messageColor, informationColor, messageText, informationText, new ImageIcon(pictureURL), LocalDateTime.now(), 30, 5);
                }

                //create request
                Request billboardRequest = new Request(CREATE_BILLBOARD, billboard, null);

                frame.setContentPane(new RenderedBillboard(billboard, new Dimension(900, 500)));
                frame.pack();
                frame.setLocationRelativeTo(null);
                frame.setVisible(true);

            }
        });
    }

    private void importXML(File xmlFile) {

    }

    private static String encodeFileToBase64Binary(File file) throws IOException {
        byte[] bytes = loadFile(file);
        byte[] encoded = Base64.getEncoder().encode(bytes);
        String encodedString = new String(encoded, StandardCharsets.US_ASCII);

        return encodedString;
    }

    private static byte[] loadFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);

        long length = file.length();
        if (length > Integer.MAX_VALUE) {
            // File is too large
        }
        byte[] bytes = new byte[(int)length];

        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
                && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }

        if (offset < bytes.length) {
            throw new IOException("Could not completely read file "+file.getName());
        }

        is.close();
        return bytes;
    }
}