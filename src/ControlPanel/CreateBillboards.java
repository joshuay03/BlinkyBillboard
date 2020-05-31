package ControlPanel;

import BillboardSupport.Billboard;
import BillboardSupport.RenderedBillboard;
import Client.ClientConnector;
import SocketCommunication.Request;
import SocketCommunication.Response;
import org.w3c.dom.Document;

import javax.swing.*;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.filechooser.FileNameExtensionFilter;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.MalformedURLException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.util.Arrays;
import java.util.Base64;

/**
 * A class to represent a "Create Billboards" page which is bound to CreateBillboards.form
 */
public class CreateBillboards {
    protected JPanel createBillboardsPanel;
    protected JPanel titlePanel;
    protected JButton backButton;
    protected JLabel createBillboardsLabel;
    protected JPanel optionPanel;
    protected JButton importButton;
    protected JButton exportButton;
    protected JButton previewBillboardButton;
    protected JPanel createPanel;
    protected JLabel nameLabel;
    protected JTextArea nameTextArea;
    protected JLabel messageLabel;
    protected JTextArea messageTextArea;
    protected JPanel messageColourPanel;
    protected JButton messageColourButton;
    protected JButton pictureButton;
    protected JLabel pictureURLLabel;
    protected JFormattedTextField pictureURLFormattedTextField;
    protected JLabel informationLabel;
    protected JTextArea informationTextArea;
    protected JPanel informationColourPanel;
    protected JButton informationColourButton;
    protected JPanel backgroundColourPanel;
    protected JButton backgroundColourButton;
    protected JButton saveBillboardButton;

    protected ColourChooser messageColourChooser = new ColourChooser(Color.BLACK);
    protected ColourChooser informationColourChooser = new ColourChooser(Color.BLACK);
    protected ColourChooser backgroundColourChooser = new ColourChooser(Color.WHITE);
    protected Billboard billboard;
    protected JFrame previewFrame;

    /**
     * Instantiates the frame for creating a new billboard
     * @param frame A JFrame object
     * @param connector A ClientConnector object
     */
    public CreateBillboards(JFrame frame, ClientConnector connector) {
        billboard = new Billboard();

        messageColourPanel.setBackground(Color.BLACK);
        informationColourPanel.setBackground(Color.BLACK);
        backgroundColourPanel.setBackground(Color.WHITE);

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
                    billboard = Billboard.getBillboardFromXML(xmlFile);
                    assert billboard != null;
                    messageTextArea.setText(billboard.getMessage());
                    informationTextArea.setText(billboard.getInformation());

                    if(billboard.getImageURL() != null) pictureURLFormattedTextField.setText(billboard.getImageURL().toString());
                }
            }
        });

        exportButton.addActionListener(new ActionListener() {
            /**
             * Invoked when an action occurs.
             *
             * @param e the event to be processed
             */
            @Override
            public void actionPerformed(ActionEvent e) {

                Document XMLRep = billboard.getXMLRepresentation();

                JFileChooser chooser = new JFileChooser();
                FileNameExtensionFilter filter = new FileNameExtensionFilter("XML", "xml");
                chooser.setFileFilter(filter);
                int returnVal = chooser.showSaveDialog(null);
                if (returnVal == JFileChooser.APPROVE_OPTION) {
                    File fileLocation = chooser.getSelectedFile();

                    try {
                        // create the xml file
                        //transform the DOM Object to an XML File
                        TransformerFactory transformerFactory = TransformerFactory.newInstance();
                        Transformer transformer = transformerFactory.newTransformer();
                        DOMSource domSource = new DOMSource(XMLRep);
                        StreamResult streamResult = new StreamResult(fileLocation);

                        transformer.transform(domSource, streamResult);
                    } catch (TransformerException ex) {
                        ex.printStackTrace();
                    }
                }
            }
        });

        previewBillboardButton.addActionListener(new ActionListener() {
            /**
             * Invoked when an action occurs.
             *
             * @param e the event to be processed
             */
            @Override
            public void actionPerformed(ActionEvent e) {
                Dimension renderDimension = new Dimension((int)Toolkit.getDefaultToolkit().getScreenSize().getWidth()/2,
                        (int)Toolkit.getDefaultToolkit().getScreenSize().getHeight()/2);
                RenderedBillboard renderedBillboard = new RenderedBillboard(billboard, renderDimension);

                previewFrame = new JFrame("Preview: " + nameTextArea.getText());

                previewFrame.setSize(renderDimension);
                previewFrame.setContentPane(renderedBillboard);
                previewFrame.setVisible(true);
            }
        });

        messageTextArea.getDocument().addDocumentListener(new DocumentListener() {
            /**
             * Invoked when an action occurs.
             * Set the message data of the billboard
             * @param e the event to be processed
             */
            @Override
            public void insertUpdate(DocumentEvent e) {
                billboard.setMessage(messageTextArea.getText());
            }

            /**
             * Invoked when an action occurs.
             * Remove the message data of the billboard
             * @param e the event to be processed
             */
            @Override
            public void removeUpdate(DocumentEvent e) {
                billboard.setMessage(messageTextArea.getText());
            }

            /**
             * Invoked when an action occurs.
             * Change the message data of the billboard
             * @param e the event to be processed
             */
            @Override
            public void changedUpdate(DocumentEvent e) {
                billboard.setMessage(messageTextArea.getText());
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
                int res = JOptionPane.showOptionDialog(frame, messageColourChooser, "Choose colour", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);
                if (res == 0) {
                    messageColourPanel.setBackground(messageColourChooser.getColor());
                    billboard.setMessageColour(messageColourChooser.getColor());
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
                        byte[] imageData = Files.readAllBytes(pictureFile.toPath());
                        String imageString = Base64.getEncoder().encodeToString(imageData);
                        billboard.setImageData(imageString);
                    } catch (IOException ioException) {
                        ioException.printStackTrace();
                    }
                }
            }
        });

        pictureURLFormattedTextField.getDocument().addDocumentListener(new DocumentListener() {
            /**
             * Invoked when an action occurs.
             * Set the image of the billboard
             * @param e the event to be processed
             */
            @Override
            public void insertUpdate(DocumentEvent e) {
                try {
                    billboard.setImageURL(new URL(pictureURLFormattedTextField.getText()));
                    billboard.setImageData(null);
                } catch (MalformedURLException malformedURLException) {
                    malformedURLException.printStackTrace();
                }
            }

            /**
             * Invoked when an action occurs.
             * Remove the image of the billboard
             * @param e the event to be processed
             */
            @Override
            public void removeUpdate(DocumentEvent e) {
                try {
                    billboard.setImageURL(new URL(pictureURLFormattedTextField.getText()));
                    billboard.setImageData(null);
                } catch (MalformedURLException malformedURLException) {
                    malformedURLException.printStackTrace();
                }
            }

            /**
             * Invoked when an action occurs.
             * Change the image of the billboard
             * @param e the event to be processed
             */
            @Override
            public void changedUpdate(DocumentEvent e) {
                try {
                    billboard.setImageURL(new URL(pictureURLFormattedTextField.getText()));
                    billboard.setImageData(null);
                } catch (MalformedURLException malformedURLException) {
                    malformedURLException.printStackTrace();
                }
            }
        });

        informationTextArea.getDocument().addDocumentListener(new DocumentListener() {
            /**
             * Invoked when an action occurs.
             *  Set the information data.
             * @param e the event to be processed
             */
            @Override
            public void insertUpdate(DocumentEvent e) {
                billboard.setInformation(informationTextArea.getText());
            }

            /**
             * Invoked when an action occurs.
             * Delete the information data.
             * @param e the event to be processed
             */
            @Override
            public void removeUpdate(DocumentEvent e) {
                billboard.setInformation(informationTextArea.getText());
            }

            /**
             * Invoked when an action occurs.
             * Change the information data.
             * @param e the event to be processed
             */
            @Override
            public void changedUpdate(DocumentEvent e) {
                billboard.setInformation(informationTextArea.getText());
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
                int res = JOptionPane.showOptionDialog(frame, informationColourChooser, "Choose colour", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);
                if (res == 0) {
                    informationColourPanel.setBackground(informationColourChooser.getColor());
                    billboard.setInformationColour(informationColourChooser.getColor());
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
                int res = JOptionPane.showOptionDialog(frame, backgroundColourChooser, "Choose colour", JOptionPane.DEFAULT_OPTION, JOptionPane.INFORMATION_MESSAGE, null, null, null);
                if (res == 0) {
                    backgroundColourPanel.setBackground(backgroundColourChooser.getColor());
                    billboard.setBackgroundColour(backgroundColourChooser.getColor());
                }
            }
        });

        saveBillboardButton.addActionListener(new ActionListener() {
            /**
             * Invoked when an action occurs.
             *
             * @param e the event to be processed
             */
            @Override
            public void actionPerformed(ActionEvent e) {

                // Check whether the string is empty
                if (nameTextArea.getText().length() < 1){
                    JOptionPane.showMessageDialog(frame, "Cannot create billboard without a name.");
                    return;
                } else {
                    billboard.setBillboardName(nameTextArea.getText());
                }
                //create request
                Request createBillboard = Request.createBillboardReq(billboard, connector.session);

                // Send request to server
                Response response;

                try {
                    response = createBillboard.Send(connector);
                } catch (IOException excep) {
                    JOptionPane.showMessageDialog(frame, "Cannot save billboard on server.");
                    return;
                }

                // check status of response
                boolean status = response.isStatus();

                if (!status) {
                    String errorMsg = (String) response.getData();
                    JOptionPane.showMessageDialog(frame, "Cannot save billboard on server. Error: " + errorMsg);
                }

                if (status) {
                    JOptionPane.showMessageDialog(frame, response.getData());
                    frame.setContentPane(new OptionMenu(frame, connector).optionMenuPanel);
                    frame.pack();
                    frame.setLocationRelativeTo(null);
                    frame.setVisible(true);
                }
            }
        });
    }

    /**
     * Encode a gien file to base64 text
     * @param file
     * @return the encoded text
     * @throws IOException
     */
    private static String encodeFileToBase64Binary(File file) throws IOException {
        byte[] bytes = loadFile(file);
        byte[] encoded = Base64.getEncoder().encode(bytes);
        String encodedString = new String(encoded, StandardCharsets.US_ASCII);

        return encodedString;
    }

    /**
     * Loads a given file, rreads the file into a byte array.
     * @param file
     * @return A byte aray of the file contents.
     * @throws IOException
     */
    private static byte[] loadFile(File file) throws IOException {
        InputStream is = new FileInputStream(file);

        long length = file.length();
        if (length > Integer.MAX_VALUE) {
            JOptionPane.showMessageDialog(null, "File is too large", "Error", JOptionPane.DEFAULT_OPTION);
        }
        byte[] bytes = new byte[(int)length];

        int offset = 0;
        int numRead = 0;
        while (offset < bytes.length
                && (numRead=is.read(bytes, offset, bytes.length-offset)) >= 0) {
            offset += numRead;
        }

        if (offset < bytes.length) {
            throw new IOException("Could not completely read file " + file.getName());
        }

        is.close();
        return bytes;
    }
}