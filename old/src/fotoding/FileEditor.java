/**
 * @author Rickhall ... changed and adapted by POC
 * 
 * This component's use is more flexible than the SWING's
 * JFileChooser and it fits better to my photo programme ...
 *
 */
package fotoding;

import java.awt.*;
import java.awt.event.*;
import javax.swing.*;

public class FileEditor extends JPanel {
	private String selection = null;
	private JTextField textField = null;
	private JButton browseButton = null;
	private ImageIcon bild=null;
	private boolean directory = false;

	/**
	 * Constructor for FileEditor.
	 */
	public FileEditor(boolean directory) 	{
		super();
		this.directory = directory;
		init();
	}

	public FileEditor(boolean directory,ImageIcon icon) 	{
		super();
		this.bild=icon;
		this.directory = directory;
		init();
	}

	public void setEnabled(boolean b) 	{
		textField.setEnabled(b);
		browseButton.setEnabled(b);
	}

	/**
	 * as long as there has not occured any selection
	 * it's usually not of use to go on with the programme
	 */
	public boolean isSelected() {
		return (selection==null);
	} // end of isSelected


	protected void init() 	{
		// Set layout.
		GridBagLayout grid = new GridBagLayout();
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.insets = new Insets(0, 2, 0, 2);
		setLayout(grid);

		// Add field.
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.gridheight = 1;
		gbc.gridwidth = 2;
		gbc.anchor = GridBagConstraints.WEST;
		textField = new JTextField(60);
		textField.setText("Bitte Quellverzeichnis auswählen");
		grid.setConstraints(textField, gbc);
		add(textField);

		// Add button.
		gbc.gridx = 2;
		gbc.gridy = 0;
		gbc.gridheight = 1;
		gbc.gridwidth = 1;
		gbc.anchor = GridBagConstraints.EAST;
		
		// mit Button ?
		if(this.bild==null) {
			browseButton = new JButton("Verzeichnisauswahl");
		} else {
			browseButton = new JButton("Verzeichnisauswahl",this.bild);
		}
		
		browseButton.setMnemonic('v');
		browseButton.setMargin(new Insets(1, 1, 1, 1));
		grid.setConstraints(browseButton, gbc);
		add(browseButton);

		// Add focus listener.
		textField.addFocusListener(new FocusListener() {
			public void focusGained(FocusEvent event) 			{
			}
			public void focusLost(FocusEvent event) 			{
				if (!event.isTemporary()) 				{

					/** i am not really sure what is going on here
					 * but i followed rick's implementation
					 */
					selection = new String(textField.getText());
				}
			}
		});

		// Add action listener.
		browseButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent event) 			{
				JFileChooser fileDlg = new JFileChooser();
				if (directory) 				{
					fileDlg.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
					fileDlg.setDialogTitle("Bitte Verzeichnis auswählen.");
				} else {
					fileDlg.setFileSelectionMode(JFileChooser.FILES_ONLY);
					fileDlg.setDialogTitle("Bitte eine Datei auswählen");
				}
				fileDlg.setApproveButtonText("Auswählen");
				if (fileDlg.showOpenDialog(FileEditor.this) ==
					JFileChooser.APPROVE_OPTION) 	{
					textField.setText(fileDlg.getSelectedFile().getAbsolutePath());
					selection = new String(textField.getText());                    
				}
			}
		});
	}

	public String toString() {
		if(this.selection==null) 
			return "(keine Auswahl)";
		else
			return this.selection;
	} // end of toString

	public String getSelection() {
		return toString();
	} // end of getSelection
} // endofclass
