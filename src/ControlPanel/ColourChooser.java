package ControlPanel;

import javax.swing.*;
import javax.swing.colorchooser.AbstractColorChooserPanel;
import javax.swing.colorchooser.DefaultColorSelectionModel;
import java.awt.*;

/**
 * A custom JColorChooser class
 */
public class ColourChooser extends JColorChooser {

    /**
     * Constructs a ColourChooser which only contains the "Swatches" panel
     */
    public ColourChooser(Color colour) {
        super(new DefaultColorSelectionModel(colour));

        AbstractColorChooserPanel[] panels = super.getChooserPanels();
        for (AbstractColorChooserPanel panel : panels) {
            if (panel.getDisplayName().equals("Swatches")) {
                super.setChooserPanels(new AbstractColorChooserPanel[] {panel});
            }
        }

        super.setPreviewPanel(new JPanel());
    }
}